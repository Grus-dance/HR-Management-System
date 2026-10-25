/**
 * 用系统 Edge 无头浏览器检查全部页面：截图 + 控制台错误 + 横向溢出检查。
 * 运行：node tools/browser/check.mjs
 */
import { chromium } from 'playwright-core';
import { mkdirSync, writeFileSync } from 'node:fs';

const BASE = 'http://localhost:8080';
const OUT = 'docs/screenshots';
mkdirSync(OUT, { recursive: true });

const issues = [];
const checked = [];

const browser = await chromium.launch({
    executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',
    headless: true,
    args: ['--disable-gpu']
});

function watch(page) {
    page.on('console', (message) => {
        if (message.type() !== 'error') return;
        // 登录页探测会话返回 401、改密弹窗故意试错返回 400，均属预期
        if (message.text().includes('status of 401') || message.text().includes('status of 400')) return;
        issues.push(`console: ${message.text()} @ ${page.url()}`);
    });
    page.on('pageerror', (error) => issues.push(`pageerror: ${error.message} @ ${page.url()}`));
    page.on('response', (response) => {
        if (response.status() >= 400 && !response.url().includes('favicon')) {
            if (response.status() === 401 && response.url().includes('/api/auth/me')) return;
            if (response.status() === 400 && response.url().includes('/api/auth/password')) return;
            issues.push(`http ${response.status()}: ${response.url()}`);
        }
    });
}

async function login(empNo, password = '123456') {
    const context = await browser.newContext({
        viewport: { width: 1440, height: 900 },
        locale: 'zh-CN',
        timezoneId: 'Asia/Shanghai'
    });
    const page = await context.newPage();
    watch(page);
    await page.goto(`${BASE}/login.html`, { waitUntil: 'networkidle' });
    await page.fill('#empNo', empNo);
    await page.fill('#password', password);
    await page.click('#login-button');
    await page.waitForURL((url) => !url.pathname.endsWith('/login.html'), { timeout: 15000 });
    return { context, page };
}

async function open(page, path, name, options = {}) {
    await page.goto(BASE + path, { waitUntil: 'networkidle' });
    if (options.waitFor) {
        await page.waitForSelector(options.waitFor, { timeout: 15000 }).catch(() => {
            issues.push(`waitFor timeout: ${options.waitFor} @ ${path}`);
        });
    }
    await page.waitForTimeout(700);
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
    if (overflow > 2) issues.push(`horizontal overflow ${overflow}px @ ${path}`);
    await page.screenshot({ path: `${OUT}/${name}.png`, fullPage: true });
    checked.push(`${path} -> ${name}.png`);
}

// 1. 登录页
{
    const context = await browser.newContext({ viewport: { width: 1440, height: 900 }, locale: 'zh-CN' });
    const page = await context.newPage();
    watch(page);
    await page.goto(`${BASE}/login.html`, { waitUntil: 'networkidle' });
    await page.screenshot({ path: `${OUT}/01-login.png`, fullPage: true });
    checked.push('/login.html -> 01-login.png');
    await context.close();
}

// 2. 管理者
{
    const { context, page } = await login('M0001');
    await open(page, '/manager/dashboard.html', '02-manager-dashboard', { waitFor: '#dept-chart canvas' });

    // 顶部工作台与个人信息弹窗
    const navCount = await page.$$eval('.topnav-item', (items) => items.length);
    if (navCount !== 5) issues.push(`manager topnav items=${navCount}（期望 5）`);
    const activeText = await page.$eval('.topnav-item.active', (el) => el.textContent.trim()).catch(() => '');
    if (!activeText.includes('概览')) issues.push(`topnav active="${activeText}"`);

    await page.click('#user-trigger');
    await page.waitForSelector('.modal-mask .modal', { timeout: 10000 });
    await page.waitForTimeout(300);
    await page.screenshot({ path: `${OUT}/17-user-panel.png`, fullPage: true });
    checked.push('/manager/dashboard.html (个人信息弹窗) -> 17-user-panel.png');

    await page.click('#user-change-password');
    await page.waitForSelector('#modal-old-password', { timeout: 10000 });
    await page.waitForTimeout(300);
    await page.screenshot({ path: `${OUT}/18-change-password-modal.png`, fullPage: true });
    checked.push('/manager/dashboard.html (修改密码弹窗) -> 18-change-password-modal.png');

    await page.fill('#modal-old-password', '000000');
    await page.fill('#modal-new-password', 'test1234');
    await page.fill('#modal-confirm-password', 'test1234');
    await page.click('.modal-foot .btn-primary');
    await page.waitForTimeout(800);
    const pwdError = await page.textContent('#modal-password-error').catch(() => '');
    if (!pwdError || !pwdError.includes('原密码')) issues.push(`password modal error="${pwdError}"`);
    await page.keyboard.press('Escape');

    await open(page, '/manager/employees.html', '03-manager-employees', { waitFor: '#employee-body tr' });
    await page.click('#employee-body [data-detail]');
    await page.waitForSelector('.modal-mask .modal', { timeout: 10000 });
    await page.waitForTimeout(400);
    await page.screenshot({ path: `${OUT}/03b-manager-employee-detail.png`, fullPage: true });
    checked.push('/manager/employees.html (详情弹窗) -> 03b-manager-employee-detail.png');
    await page.keyboard.press('Escape');

    await open(page, '/manager/salary.html', '04-manager-salary', { waitFor: '#salary-body tr[data-row]' });
    await page.click('#salary-body [data-expand]');
    await page.waitForSelector('.expand-row', { timeout: 10000 });
    await page.waitForTimeout(500);
    await page.screenshot({ path: `${OUT}/05-manager-salary-expanded.png`, fullPage: true });
    checked.push('/manager/salary.html (展开) -> 05-manager-salary-expanded.png');

    await open(page, '/manager/bonus.html', '06-manager-bonus', { waitFor: '#employee-body tr' });
    await open(page, '/manager/analytics.html', '07-manager-analytics', { waitFor: '#salary-chart canvas' });
    await context.close();
}

// 3. 一般用户
{
    const { context, page } = await login('H0001');
    await open(page, '/hr/employees.html', '08-hr-employees', { waitFor: '#employee-body tr' });
    await open(page, '/hr/salary.html', '09-hr-salary', { waitFor: '#employee-body tr' });
    await page.click('#employee-body [data-adjust]');
    await page.waitForSelector('.modal-mask .modal', { timeout: 10000 });
    await page.waitForTimeout(400);
    await page.screenshot({ path: `${OUT}/09b-hr-salary-adjust.png`, fullPage: true });
    checked.push('/hr/salary.html (调整弹窗) -> 09b-hr-salary-adjust.png');
    await page.keyboard.press('Escape');
    await open(page, '/hr/performance.html', '10-hr-performance', { waitFor: '#performance-body tr' });
    await context.close();
}

// 4. 个人用户
{
    const { context, page } = await login('X1002');
    await open(page, '/me/dashboard.html', '11-me-dashboard', { waitFor: '#performance-body tr' });
    await open(page, '/me/profile.html', '12-me-profile', { waitFor: '#basic-info .detail-item' });
    await open(page, '/me/salary.html', '13-me-salary', { waitFor: '#salary-chart canvas' });
    await open(page, '/me/performance.html', '14-me-performance', { waitFor: '#performance-body tr' });
    await page.click('#submit-button');
    await page.waitForSelector('.modal-mask .modal', { timeout: 10000 });
    await page.waitForTimeout(400);
    await page.screenshot({ path: `${OUT}/14b-me-performance-form.png`, fullPage: true });
    checked.push('/me/performance.html (提交表单) -> 14b-me-performance-form.png');
    await page.keyboard.press('Escape');
    await open(page, '/me/bonus.html', '15-me-bonus', { waitFor: '#packet-list .stat-grid, #packet-list .empty' });

    const pdf = await context.request.get(`${BASE}/api/me/resume.pdf`);
    if (pdf.status() !== 200) issues.push(`employee resume status=${pdf.status()}`);
    checked.push(`resume pdf status=${pdf.status()} type=${pdf.headers()['content-type']}`);
    await context.close();
}

// 5. 首次改密
{
    const { context, page } = await login('X1001');
    await page.waitForURL('**/me/password.html', { timeout: 10000 });
    await page.waitForTimeout(500);
    await page.screenshot({ path: `${OUT}/16-first-login-password.png`, fullPage: true });
    checked.push('/me/password.html -> 16-first-login-password.png');
    await context.close();
}

await browser.close();

const report = [
    `pages: ${checked.length}`,
    ...checked.map((line) => '  ' + line),
    '',
    `issues: ${issues.length}`,
    ...issues.map((line) => '  ' + line)
].join('\n');
writeFileSync('tools/browser/check-result.txt', report + '\n', 'utf8');
console.log(report);
