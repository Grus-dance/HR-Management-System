/* 公共布局：B 站风格顶部工作台导航 + 右上角个人信息弹窗 */
(function (global) {
    const HOME = {
        MANAGER: '/manager/dashboard.html',
        HR: '/hr/employees.html',
        EMPLOYEE: '/me/dashboard.html'
    };

    const ICONS = {
        dashboard: '<rect x="3" y="3" width="7" height="7" rx="1.6"/><rect x="14" y="3" width="7" height="7" rx="1.6"/>' +
            '<rect x="3" y="14" width="7" height="7" rx="1.6"/><rect x="14" y="14" width="7" height="7" rx="1.6"/>',
        users: '<circle cx="9" cy="8" r="3.2"/><path d="M3.5 20c0-3 2.5-5 5.5-5s5.5 2 5.5 5"/>' +
            '<path d="M16 5.2a3 3 0 0 1 0 5.6"/><path d="M17.2 15.3c2 .6 3.3 2.2 3.3 4.7"/>',
        wallet: '<rect x="3" y="6" width="18" height="13" rx="2.4"/><path d="M3 10h18"/>' +
            '<path d="M16 14.5h2.5"/>',
        gift: '<rect x="3.5" y="9" width="17" height="11.5" rx="2"/><path d="M3.5 13.2h17"/>' +
            '<path d="M12 9v11.5"/><path d="M12 9c-1.8 0-4.6-.6-4.6-2.8C7.4 4.9 9 4 10.2 4.6c1.4.8 1.8 2.6 1.8 4.4Z"/>' +
            '<path d="M12 9c1.8 0 4.6-.6 4.6-2.8C16.6 4.9 15 4 13.8 4.6c-1.4.8-1.8 2.6-1.8 4.4Z"/>',
        chart: '<path d="M4 4v15.5h16"/><path d="M7.5 15.5l3.4-4.2 3 2.3 4.6-6"/>',
        user: '<circle cx="12" cy="8" r="3.6"/><path d="M5 20.5c0-3.6 3.1-6 7-6s7 2.4 7 6"/>',
        target: '<circle cx="12" cy="12" r="8"/><circle cx="12" cy="12" r="4"/><circle cx="12" cy="12" r="1"/>',
        lock: '<rect x="5" y="10.5" width="14" height="9.5" rx="2"/><path d="M8.5 10.5V8a3.5 3.5 0 0 1 7 0v2.5"/>',
        logout: '<path d="M15 4h3.5A1.5 1.5 0 0 1 20 5.5v13a1.5 1.5 0 0 1-1.5 1.5H15"/>' +
            '<path d="M10 8l-4 4 4 4"/><path d="M6 12h9"/>',
        trend: '<path d="M4 17l5-5 4 3 7-8"/><path d="M20 7h-4"/><path d="M20 7v4"/>',
        credit: '<rect x="3" y="6" width="18" height="12" rx="2"/><path d="M3 10h18"/><path d="M7 14h4"/>',
        clipboard: '<rect x="6" y="5" width="12" height="16" rx="2"/><path d="M9 5V3h6v2"/>' +
            '<path d="M9 10h6M9 14h4"/>'
    };

    const MENUS = {
        MANAGER: [
            { href: '/manager/dashboard.html', icon: 'dashboard', label: '概览' },
            { href: '/manager/employees.html', icon: 'users', label: '员工档案' },
            { href: '/manager/salary.html', icon: 'wallet', label: '薪酬等级调整' },
            { href: '/manager/bonus.html', icon: 'gift', label: '绩效与红包' },
            { href: '/manager/analytics.html', icon: 'chart', label: '数据分析' }
        ],
        HR: [
            { href: '/hr/employees.html', icon: 'users', label: '员工管理' },
            { href: '/hr/salary.html', icon: 'wallet', label: '薪酬调整' },
            { href: '/hr/performance.html', icon: 'target', label: '业绩审核' }
        ],
        EMPLOYEE: [
            { href: '/me/dashboard.html', icon: 'dashboard', label: '我的概览' },
            { href: '/me/profile.html', icon: 'user', label: '我的信息' },
            { href: '/me/salary.html', icon: 'wallet', label: '我的薪资' },
            { href: '/me/performance.html', icon: 'target', label: '我的业绩' },
            { href: '/me/bonus.html', icon: 'gift', label: '我的红包' }
        ]
    };

    function icon(name) {
        const body = ICONS[name] || ICONS.dashboard;
        return '<svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" ' +
            'stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' + body + '</svg>';
    }

    function home(role) {
        return HOME[role] || '/login.html';
    }

    function renderTopnav(me) {
        const topnav = document.getElementById('topnav');
        if (!topnav) return;
        const path = location.pathname;
        const items = me.mustChangePassword ? '' : (MENUS[me.role] || []).map((item) =>
            '<a class="topnav-item ' + (path === item.href ? 'active' : '') + '" href="' + item.href + '">' +
            icon(item.icon) + '<span>' + item.label + '</span></a>'
        ).join('');

        topnav.innerHTML =
            '<div class="topnav-inner">' +
            '  <a class="brand" href="' + home(me.role) + '">' +
            '    <img class="brand-mark" src="/assets/img/logo.svg" alt="">' +
            '    <div class="brand-text"><strong>X 公司人事管理</strong><span>HR MANAGEMENT</span></div>' +
            '  </a>' +
            '  <nav class="topnav-menu" aria-label="工作台">' + items + '</nav>' +
            '  <div class="topnav-right">' +
            '    <button class="user-trigger" id="user-trigger" type="button" aria-haspopup="dialog" aria-label="账号信息">' +
            '      <span class="avatar">' + UI.escapeHtml(String(me.name || '?').slice(0, 1)) + '</span>' +
            '      <span class="user-meta"><strong>' + UI.escapeHtml(me.name) + '</strong>' +
            '      <small>' + UI.escapeHtml(me.roleLabel) + ' · ' + UI.escapeHtml(me.empNo) + '</small></span>' +
            '      <span class="user-caret">▾</span>' +
            '    </button>' +
            '  </div>' +
            '</div>';

        document.getElementById('user-trigger').addEventListener('click', () => openUserPanel(me));
    }

    function openUserPanel(me) {
        const handle = UI.openModal({
            title: '账号信息',
            size: 'sm',
            footer: false,
            content:
                '<div class="user-panel">' +
                '  <div class="avatar">' + UI.escapeHtml(String(me.name || '?').slice(0, 1)) + '</div>' +
                '  <div><h4>' + UI.escapeHtml(me.name) + '</h4>' +
                '  <p>' + UI.escapeHtml(me.roleLabel) + ' · ' + UI.escapeHtml(me.empNo) + '</p></div>' +
                '</div>' +
                '<div class="user-links">' +
                '  <button class="user-link" id="user-change-password" type="button">' +
                '    <span>' + icon('lock') + '修改密码</span><span class="arrow">›</span></button>' +
                '  <button class="user-link danger" id="user-logout" type="button">' +
                '    <span>' + icon('logout') + '退出登录</span><span class="arrow">›</span></button>' +
                '</div>'
        });

        handle.body.querySelector('#user-change-password').addEventListener('click', () => {
            handle.close();
            openChangePassword(me);
        });
        handle.body.querySelector('#user-logout').addEventListener('click', () => {
            handle.close();
            logout();
        });
    }

    function openChangePassword(me) {
        const handle = UI.openModal({
            title: '修改密码',
            content:
                '<div class="field"><label for="modal-old-password">原密码</label>' +
                '<input id="modal-old-password" type="password" maxlength="32" autocomplete="current-password" placeholder="请输入原密码"></div>' +
                '<div class="field" style="margin-top:12px"><label for="modal-new-password">新密码</label>' +
                '<input id="modal-new-password" type="password" maxlength="32" autocomplete="new-password" placeholder="请输入新密码">' +
                '<span class="form-hint">6-32 位，需同时包含字母和数字</span></div>' +
                '<div class="field" style="margin-top:12px"><label for="modal-confirm-password">确认新密码</label>' +
                '<input id="modal-confirm-password" type="password" maxlength="32" autocomplete="new-password" placeholder="再次输入新密码"></div>' +
                '<div class="form-error" id="modal-password-error" style="margin-top:8px"></div>'
        });

        const cancel = document.createElement('button');
        cancel.className = 'btn';
        cancel.type = 'button';
        cancel.textContent = '取消';
        cancel.addEventListener('click', handle.close);

        const save = document.createElement('button');
        save.className = 'btn btn-primary';
        save.type = 'button';
        save.textContent = '保存新密码';
        save.addEventListener('click', async () => {
            const oldPassword = document.getElementById('modal-old-password').value;
            const newPassword = document.getElementById('modal-new-password').value;
            const confirmPassword = document.getElementById('modal-confirm-password').value;
            const errorBox = document.getElementById('modal-password-error');
            errorBox.textContent = '';
            if (!oldPassword || !newPassword) {
                errorBox.textContent = '请填写原密码和新密码';
                return;
            }
            if (newPassword !== confirmPassword) {
                errorBox.textContent = '两次输入的新密码不一致';
                return;
            }
            if (!/^(?=.*[A-Za-z])(?=.*\d).+$/.test(newPassword)) {
                errorBox.textContent = '新密码需同时包含字母和数字';
                return;
            }
            save.disabled = true;
            try {
                await Api.put('/api/auth/password', { oldPassword, newPassword });
                UI.toast('密码修改成功', 'success');
                handle.close();
                if (me && me.mustChangePassword) {
                    location.href = home(me.role);
                }
            } catch (err) {
                errorBox.textContent = err.message || '修改失败';
                save.disabled = false;
            }
        });

        handle.foot.appendChild(cancel);
        handle.foot.appendChild(save);
    }

    async function logout() {
        const confirmed = await UI.confirmDialog('确定退出当前账号吗？', { confirmText: '退出登录', danger: true });
        if (!confirmed) return;
        try {
            await Api.post('/api/auth/logout');
        } catch (err) {
            /* 会话已失效也继续跳转 */
        }
        location.href = '/login.html';
    }

    async function init() {
        const me = await Api.get('/api/auth/me');
        if (me.mustChangePassword && !location.pathname.endsWith('/password.html')) {
            location.replace('/me/password.html');
            return new Promise(() => {});
        }
        renderTopnav(me);
        return me;
    }

    global.Layout = { init, home, renderTopnav, openUserPanel, openChangePassword, icon };
})(window);
