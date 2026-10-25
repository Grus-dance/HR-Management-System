/* 统一 API 封装：处理 JSON、错误码、401 跳转与首次改密拦截 */
(function (global) {
    async function request(url, options) {
        const opts = Object.assign({ method: 'GET', headers: {} }, options || {});
        if (opts.body !== undefined && opts.body !== null) {
            opts.headers['Content-Type'] = 'application/json;charset=UTF-8';
            opts.body = typeof opts.body === 'string' ? opts.body : JSON.stringify(opts.body);
        }
        let response;
        try {
            response = await fetch(url, opts);
        } catch (err) {
            throw new Error('网络异常，请确认服务已启动');
        }
        if (response.status === 401) {
            if (!location.pathname.endsWith('/login.html')) {
                location.href = '/login.html';
            }
            throw new Error('登录已失效，请重新登录');
        }
        const text = await response.text();
        let payload = null;
        if (text) {
            try { payload = JSON.parse(text); } catch (err) { payload = null; }
        }
        if (!response.ok || (payload && payload.code !== 0)) {
            const code = payload ? payload.code : response.status;
            const message = (payload && payload.message) || ('请求失败（' + response.status + '）');
            if (code === 40302 && !location.pathname.endsWith('/password.html')) {
                location.href = '/me/password.html';
            }
            const error = new Error(message);
            error.code = code;
            error.status = response.status;
            throw error;
        }
        return payload ? payload.data : null;
    }

    global.Api = {
        get: (url) => request(url),
        post: (url, body) => request(url, { method: 'POST', body: body === undefined ? null : body }),
        put: (url, body) => request(url, { method: 'PUT', body: body === undefined ? null : body }),
        del: (url) => request(url, { method: 'DELETE' })
    };
})(window);
