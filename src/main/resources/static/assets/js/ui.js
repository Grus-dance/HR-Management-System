/* 通用 UI 工具：格式化、提示、弹窗、分页、状态标签 */
(function (global) {
    const $ = (selector, root) => (root || document).querySelector(selector);
    const $$ = (selector, root) => Array.from((root || document).querySelectorAll(selector));

    function escapeHtml(value) {
        if (value === null || value === undefined) return '';
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function money(value) {
        const number = Number(value || 0);
        return number.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    function percent(value) {
        return (Number(value || 0) * 100).toFixed(2).replace(/\.?0+$/, '') + '%';
    }

    function fmtDate(value) {
        return value ? String(value).slice(0, 10) : '—';
    }

    function fmtDateTime(value) {
        if (!value) return '—';
        return String(value).replace('T', ' ').slice(0, 16);
    }

    function text(value, fallback) {
        return value === null || value === undefined || value === '' ? (fallback || '—') : value;
    }

    const STATUS_MAP = {
        PENDING: ['待审核', 'tag-amber'],
        APPROVED: ['已通过', 'tag-green'],
        REJECTED: ['已驳回', 'tag-red'],
        ACTIVE: ['在职', 'tag-green'],
        RESIGNED: ['离职', 'tag-gray']
    };

    function statusTag(status) {
        const item = STATUS_MAP[status] || [status || '—', 'tag-gray'];
        return '<span class="tag ' + item[1] + '">' + escapeHtml(item[0]) + '</span>';
    }

    function toast(message, type) {
        let stack = $('.toast-stack');
        if (!stack) {
            stack = document.createElement('div');
            stack.className = 'toast-stack';
            document.body.appendChild(stack);
        }
        const item = document.createElement('div');
        item.className = 'toast ' + (type || 'info');
        item.textContent = message;
        stack.appendChild(item);
        setTimeout(() => {
            item.style.opacity = '0';
            item.style.transition = 'opacity .2s';
            setTimeout(() => item.remove(), 220);
        }, 2800);
    }

    function openModal(options) {
        const opts = options || {};
        const mask = document.createElement('div');
        mask.className = 'modal-mask';
        const size = opts.size ? ' ' + opts.size : '';
        mask.innerHTML =
            '<div class="modal' + size + '" role="dialog" aria-modal="true">' +
            '  <div class="modal-head"><h3></h3><button class="modal-x" type="button" aria-label="关闭">×</button></div>' +
            '  <div class="modal-body"></div>' +
            (opts.footer === false ? '' : '  <div class="modal-foot"></div>') +
            '</div>';
        mask.querySelector('h3').textContent = opts.title || '';
        mask.querySelector('.modal-body').innerHTML = opts.content || '';

        const close = () => {
            mask.remove();
            document.removeEventListener('keydown', onKey);
        };
        const onKey = (event) => { if (event.key === 'Escape') close(); };

        mask.querySelector('.modal-x').addEventListener('click', close);
        mask.addEventListener('mousedown', (event) => { if (event.target === mask) close(); });
        document.addEventListener('keydown', onKey);
        document.body.appendChild(mask);

        const modal = mask.querySelector('.modal');
        const body = mask.querySelector('.modal-body');
        const foot = mask.querySelector('.modal-foot');
        if (foot && typeof opts.footer === 'string') foot.innerHTML = opts.footer;
        if (opts.onMount) opts.onMount({ modal, body, foot, close });
        return { modal, body, foot, close, mask };
    }

    function confirmDialog(message, options) {
        const opts = options || {};
        return new Promise((resolve) => {
            const handle = openModal({
                title: opts.title || '请确认',
                size: 'sm',
                content: '<p class="muted" style="font-size:13.5px">' + escapeHtml(message) + '</p>'
            });
            const cancel = document.createElement('button');
            cancel.className = 'btn';
            cancel.textContent = '取消';
            cancel.addEventListener('click', () => { handle.close(); resolve(false); });
            const ok = document.createElement('button');
            ok.className = 'btn ' + (opts.danger ? 'btn-danger' : 'btn-primary');
            ok.textContent = opts.confirmText || '确定';
            ok.addEventListener('click', () => { handle.close(); resolve(true); });
            handle.foot.appendChild(cancel);
            handle.foot.appendChild(ok);
        });
    }

    function emptyRow(colspan, message) {
        return '<tr><td colspan="' + colspan + '"><div class="empty"><span class="empty-icon">∅</span>' +
            escapeHtml(message || '暂无数据') + '</div></td></tr>';
    }

    function loading(textValue) {
        return '<div class="loading">' + escapeHtml(textValue || '加载中…') + '</div>';
    }

    function renderPager(container, options) {
        const opts = options || {};
        const total = Number(opts.total || 0);
        const size = Number(opts.size || 10);
        const page = Number(opts.page || 1);
        const pages = Math.max(1, Math.ceil(total / size));
        container.innerHTML = '';
        container.className = 'pager';

        const info = document.createElement('span');
        info.textContent = '共 ' + total + ' 条 · 第 ' + page + ' / ' + pages + ' 页';
        const buttons = document.createElement('div');
        buttons.className = 'pager-btns';

        const make = (label, target, disabled, active) => {
            const button = document.createElement('button');
            button.type = 'button';
            button.textContent = label;
            if (disabled) button.disabled = true;
            if (active) button.className = 'active';
            button.addEventListener('click', () => opts.onChange && opts.onChange(target));
            return button;
        };

        buttons.appendChild(make('‹', page - 1, page <= 1));
        const start = Math.max(1, Math.min(page - 2, pages - 4));
        const end = Math.min(pages, start + 4);
        for (let i = start; i <= end; i++) {
            buttons.appendChild(make(String(i), i, false, i === page));
        }
        buttons.appendChild(make('›', page + 1, page >= pages));

        container.appendChild(info);
        container.appendChild(buttons);
    }

    function debounce(fn, wait) {
        let timer = null;
        return function () {
            const args = arguments;
            clearTimeout(timer);
            timer = setTimeout(() => fn.apply(null, args), wait || 300);
        };
    }

    global.UI = {
        $, $$, escapeHtml, money, percent, fmtDate, fmtDateTime, text,
        statusTag, toast, openModal, confirmDialog, emptyRow, loading, renderPager, debounce
    };
})(window);
