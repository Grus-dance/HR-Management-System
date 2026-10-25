/**
 * 端到端接口冒烟测试：三角色权限 + 完整业务链路。
 * 运行：node tools/smoke-test.mjs
 */
import { writeFileSync } from 'node:fs';

const BASE = 'http://localhost:8080';
const results = [];

function record(name, ok, detail) {
    results.push(`${ok ? 'PASS' : 'FAIL'} | ${name} | ${detail}`);
}

class Client {
    constructor() {
        this.cookies = new Map();
    }

    async request(path, options = {}) {
        const headers = {};
        if (options.body !== undefined) {
            headers['Content-Type'] = 'application/json; charset=utf-8';
        }
        if (this.cookies.size > 0) {
            headers['Cookie'] = [...this.cookies].map(([key, value]) => `${key}=${value}`).join('; ');
        }
        const response = await fetch(BASE + path, {
            method: options.method || 'GET',
            headers,
            body: options.body === undefined ? undefined : JSON.stringify(options.body),
            redirect: options.redirect || 'manual'
        });
        const setCookies = typeof response.headers.getSetCookie === 'function'
            ? response.headers.getSetCookie()
            : (response.headers.get('set-cookie') ? [response.headers.get('set-cookie')] : []);
        for (const cookie of setCookies) {
            const pair = cookie.split(';')[0];
            const index = pair.indexOf('=');
            if (index > 0) this.cookies.set(pair.slice(0, index), pair.slice(index + 1));
        }
        let text = '';
        try { text = await response.text(); } catch { /* 二进制响应 */ }
        let json = null;
        if (text) {
            try { json = JSON.parse(text); } catch { /* 非 JSON */ }
        }
        return { status: response.status, text, json, headers: response.headers };
    }

    async binary(path) {
        const headers = {};
        if (this.cookies.size > 0) {
            headers['Cookie'] = [...this.cookies].map(([key, value]) => `${key}=${value}`).join('; ');
        }
        const response = await fetch(BASE + path, { headers, redirect: 'manual' });
        const buffer = Buffer.from(await response.arrayBuffer());
        return { status: response.status, buffer, headers: response.headers };
    }
}

async function login(empNo, password) {
    const client = new Client();
    const response = await client.request('/api/auth/login', { method: 'POST', body: { empNo, password } });
    return { client, response };
}

// ---------- 1. 管理者 ----------
const manager = await login('M0001', '123456');
record('管理者登录', manager.response.status === 200 && manager.response.json?.data?.role === 'MANAGER',
    `status=${manager.response.status} role=${manager.response.json?.data?.role} name=${manager.response.json?.data?.name}`);

const dashboard = await manager.client.request('/api/dashboard');
record('管理者概览', dashboard.status === 200 && (dashboard.json?.data?.cards?.length || 0) >= 4,
    `cards=${dashboard.json?.data?.cards?.length}`);

const employees = await manager.client.request('/api/mgr/employees?page=1&size=10');
record('管理者员工分页', employees.status === 200 && employees.json?.data?.total === 15,
    `total=${employees.json?.data?.total} first=${employees.json?.data?.list?.[0]?.name}`);

const analytics = await manager.client.request('/api/mgr/analytics?employeeIds=4,5,7&months=6');
record('管理者数据分析', analytics.status === 200 && analytics.json?.data?.months?.length === 6,
    `series=${analytics.json?.data?.salarySeries?.length}`);

const managerToHr = await manager.client.request('/api/hr/employees');
record('管理者访问人事接口被拒', managerToHr.status === 403, `status=${managerToHr.status}`);

// ---------- 2. 一般用户建号 ----------
const hr = await login('H0001', '123456');
record('一般用户登录', hr.response.status === 200 && hr.response.json?.data?.role === 'HR',
    `status=${hr.response.status} role=${hr.response.json?.data?.role}`);

const created = await hr.client.request('/api/hr/employees', {
    method: 'POST',
    body: {
        empNo: 'X1099', name: '测试员工', gender: 'M', birthDate: '1996-06-06',
        idCard: '220102199606061234', phone: '13900001111', email: 'x1099@xcompany.com',
        address: '吉林省长春市测试路 99 号', department: '技术部', position: '开发工程师',
        gradeCode: 'G3', hireDate: '2026-01-05', education: '本科', school: '吉林大学',
        major: '软件工程', emergencyContact: '家属', emergencyPhone: '13900002222'
    }
});
record('一般用户新建用户', created.status === 200 && created.json?.data?.empNo === 'X1099',
    `status=${created.status} id=${created.json?.data?.id}`);
const testId = created.json?.data?.id;

const newEmployee = await login('X1099', '123456');
record('新用户初始密码登录', newEmployee.response.status === 200 && newEmployee.response.json?.data?.mustChangePassword === true,
    `mustChange=${newEmployee.response.json?.data?.mustChangePassword}`);

const blocked = await newEmployee.client.request('/api/me/salary');
record('未改密访问业务接口被拦截', blocked.status === 403 && blocked.json?.code === 40302,
    `status=${blocked.status} code=${blocked.json?.code}`);

const changed = await newEmployee.client.request('/api/auth/password', {
    method: 'PUT', body: { oldPassword: '123456', newPassword: 'test1234' }
});
record('首次登录修改密码', changed.status === 200, `status=${changed.status}`);

const employee = await login('X1099', 'test1234');
record('新密码登录', employee.response.status === 200 && employee.response.json?.data?.mustChangePassword === false,
    `mustChange=${employee.response.json?.data?.mustChangePassword}`);

// ---------- 3. 业绩提交 → 审核 ----------
const period = new Date().toLocaleDateString('sv-SE', { timeZone: 'Asia/Shanghai' }).slice(0, 7);
const performance = await employee.client.request('/api/me/performance', {
    method: 'POST', body: { period, score: 90, selfDesc: '完成测试任务，交付质量良好' }
});
record('员工提交业绩', performance.status === 200 && performance.json?.data?.status === 'PENDING',
    `status=${performance.status} record=${performance.json?.data?.status}`);
const performanceId = performance.json?.data?.id;

const pendingList = await hr.client.request('/api/hr/performance?status=PENDING&page=1&size=50');
const found = (pendingList.json?.data?.list || []).some((item) => item.id === performanceId);
record('人事待审列表可见', pendingList.status === 200 && found, `found=${found}`);

const reviewed = await hr.client.request(`/api/hr/performance/${performanceId}/review`, {
    method: 'POST', body: { approve: true, comment: '数据完整，确认有效' }
});
record('人事审核通过', reviewed.status === 200, `status=${reviewed.status}`);

const mine = await employee.client.request('/api/me/performance');
const approved = (mine.json?.data || []).some((item) => item.id === performanceId && item.status === 'APPROVED');
record('员工看到审核结果', approved, `approved=${approved}`);

// ---------- 4. 薪酬调整 ----------
const grade = await manager.client.request(`/api/mgr/employees/${testId}/grade`, {
    method: 'PUT', body: { gradeCode: 'G4', reason: '测试晋升，验证等级调整流程' }
});
record('管理者调整薪酬等级', grade.status === 200 && Number(grade.json?.data?.baseSalary) === 13000,
    `status=${grade.status} base=${grade.json?.data?.baseSalary}`);

const allowance = await hr.client.request(`/api/hr/employees/${testId}/salary`, {
    method: 'PUT', body: { allowance: 1000, bonusRatio: 0.2, reason: '测试津贴调整，验证流水记录' }
});
record('人事调整津贴与奖金比例', allowance.status === 200,
    `status=${allowance.status} total=${allowance.json?.data?.total}`);

// ---------- 5. 红包 ----------
const packet = await manager.client.request('/api/mgr/red-packets', {
    method: 'POST', body: { employeeId: testId, amount: 500, message: '测试红包，仅本人可见' }
});
record('管理者私发红包', packet.status === 200 && Number(packet.json?.data?.amount) === 500,
    `status=${packet.status} amount=${packet.json?.data?.amount}`);

// ---------- 6. 薪资核算 ----------
const salary = await employee.client.request('/api/me/salary');
const expectedBonus = Math.round(13000 * 0.2 * 90 / 100 * 100) / 100;
const salaryOk = salary.status === 200
    && Math.abs(Number(salary.json?.data?.performanceBonus) - expectedBonus) < 0.01
    && Math.abs(Number(salary.json?.data?.redPacket) - 500) < 0.01;
record('薪资自动核算（等级+津贴+绩效+红包）', salaryOk,
    `base=${salary.json?.data?.baseSalary} bonus=${salary.json?.data?.performanceBonus} red=${salary.json?.data?.redPacket} total=${salary.json?.data?.total}`);

// ---------- 7. 越权与离职 ----------
const employeeToHr = await employee.client.request('/api/hr/employees');
record('员工访问人事接口被拒', employeeToHr.status === 403, `status=${employeeToHr.status}`);

const employeeToManager = await employee.client.request('/api/mgr/employees');
record('员工访问管理者接口被拒', employeeToManager.status === 403, `status=${employeeToManager.status}`);

const resigned = await login('X1012', '123456');
record('离职员工登录被拒', resigned.response.status === 401, `status=${resigned.response.status}`);

const firstLogin = await login('X1001', '123456');
record('首次改密账号登录', firstLogin.response.status === 200 && firstLogin.response.json?.data?.mustChangePassword === true,
    `mustChange=${firstLogin.response.json?.data?.mustChangePassword}`);

// ---------- 8. PDF 简历 ----------
const resume = await employee.client.binary('/api/me/resume.pdf');
const head = resume.buffer.subarray(0, 5).toString('ascii');
record('PDF 简历下载', resume.status === 200 && head === '%PDF-',
    `status=${resume.status} bytes=${resume.buffer.length} disposition=${resume.headers.get('content-disposition')}`);

// ---------- 9. 页面与静态资源 ----------
const loginPage = await fetch(BASE + '/login.html', { redirect: 'manual' });
record('登录页可访问', loginPage.status === 200, `status=${loginPage.status}`);

const pageWithoutSession = await fetch(BASE + '/manager/salary.html', { redirect: 'manual' });
record('未登录访问管理页被重定向', [302, 303, 307].includes(pageWithoutSession.status),
    `status=${pageWithoutSession.status} location=${pageWithoutSession.headers.get('location')}`);

const pageResponse = await manager.client.request('/manager/salary.html');
record('管理者访问管理页', pageResponse.status === 200, `status=${pageResponse.status}`);

const css = await fetch(BASE + '/assets/css/app.css');
record('静态样式加载', css.status === 200, `status=${css.status}`);

const js = await fetch(BASE + '/assets/vendor/echarts.min.js');
record('图表库本地可用', js.status === 200, `status=${js.status} bytes=${(await js.text()).length}`);

// ---------- 输出 ----------
const failed = results.filter((line) => line.startsWith('FAIL')).length;
results.push(`SUMMARY total=${results.length - 1} failed=${failed}`);
writeFileSync('tools/smoke-result.txt', results.join('\n') + '\n', 'utf8');
console.log(results.join('\n'));
if (failed > 0) process.exitCode = 1;
