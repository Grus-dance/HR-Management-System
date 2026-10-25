/* 员工详情渲染：管理者档案、一般用户详情共用 */
(function (global) {
    function item(label, value) {
        return '<div class="detail-item"><div class="k">' + UI.escapeHtml(label) + '</div>' +
            '<div class="v">' + UI.escapeHtml(value === null || value === undefined || value === '' ? '—' : value) + '</div></div>';
    }

    function render(detail) {
        const e = detail.employee || {};
        const s = detail.salary || {};
        const experiences = detail.experiences || [];
        const performances = detail.recentPerformances || [];
        const adjustments = detail.adjustments || [];

        let html = '';
        html += '<div class="section-title">基本信息</div>';
        html += '<div class="detail-grid">';
        html += item('员工编号', e.empNo);
        html += item('姓名', e.name);
        html += item('性别', e.gender === 'M' ? '男' : '女');
        html += item('出生日期', UI.fmtDate(e.birthDate));
        html += item('身份证号', e.idCardMasked);
        html += item('手机号', e.phone);
        html += item('邮箱', e.email);
        html += item('部门', e.department);
        html += item('职位', e.position);
        html += item('薪酬等级', (e.gradeCode || '') + ' ' + (e.gradeName || ''));
        html += item('入职日期', UI.fmtDate(e.hireDate));
        html += item('在职状态', e.status === 'RESIGNED' ? '离职（' + UI.fmtDate(e.resignDate) + '）' : '在职');
        html += item('学历', e.education);
        html += item('毕业院校', e.school);
        html += item('专业', e.major);
        html += item('紧急联系人', e.emergencyContact);
        html += item('紧急联系电话', e.emergencyPhone);
        html += '</div>';

        html += '<div class="section-title">薪酬信息</div>';
        html += '<div class="detail-grid">';
        html += item('基本工资', '¥ ' + UI.money(s.baseSalary));
        html += item('岗位津贴', '¥ ' + UI.money(s.allowance));
        html += item('奖金比例', UI.percent(s.bonusRatio));
        html += item('当月绩效分', s.score === null || s.score === undefined ? '暂无有效业绩' : s.score);
        html += item('绩效奖金', '¥ ' + UI.money(s.performanceBonus));
        html += item('当月红包', '¥ ' + UI.money(s.redPacket));
        html += item('当月应发', '¥ ' + UI.money(s.total));
        html += item('薪资月份', s.month);
        html += '</div>';

        html += '<div class="section-title">曾就职经历</div>';
        if (experiences.length === 0) {
            html += '<p class="muted small">暂无曾就职记录</p>';
        } else {
            html += '<div class="timeline">';
            experiences.forEach((exp) => {
                html += '<div class="timeline-item"><div class="title">' +
                    UI.escapeHtml(exp.company) + ' · ' + UI.escapeHtml(exp.position) + '</div>' +
                    '<div class="meta">' + UI.fmtDate(exp.startDate) + ' 至 ' + UI.fmtDate(exp.endDate) + '</div>' +
                    '<div class="small muted">' + UI.escapeHtml(exp.description || '') + '</div></div>';
            });
            html += '</div>';
        }

        html += '<div class="section-title">近 3 个月业绩</div>';
        if (performances.length === 0) {
            html += '<p class="muted small">暂无业绩记录</p>';
        } else {
            html += '<table class="mini-table"><tbody>';
            performances.forEach((perf) => {
                html += '<tr><td>' + UI.escapeHtml(perf.period) + '</td>' +
                    '<td class="num">' + UI.escapeHtml(perf.score) + ' 分</td>' +
                    '<td>' + UI.statusTag(perf.status) + '</td>' +
                    '<td class="small muted">' + UI.escapeHtml(perf.reviewComment || '') + '</td></tr>';
            });
            html += '</tbody></table>';
        }

        html += '<div class="section-title">最近薪酬调整</div>';
        if (adjustments.length === 0) {
            html += '<p class="muted small">暂无调整记录</p>';
        } else {
            html += '<div class="timeline">';
            adjustments.forEach((adj) => {
                html += '<div class="timeline-item"><div class="title">' +
                    UI.escapeHtml(adj.typeLabel || adj.type) + '：' +
                    UI.escapeHtml(adj.oldValue || '—') + ' → ' + UI.escapeHtml(adj.newValue || '—') + '</div>' +
                    '<div class="meta">' + UI.fmtDateTime(adj.createdAt) + ' · 操作人 ' + UI.escapeHtml(adj.operatorName || '') + '</div>' +
                    '<div class="small muted">' + UI.escapeHtml(adj.reason || '') + '</div></div>';
            });
            html += '</div>';
        }
        return html;
    }

    global.EmployeeDetail = { render };
})(window);
