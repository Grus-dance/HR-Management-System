/** 设计规范审计：读取关键元素的最终计算样式，验证字重、字号、颜色、数字字体与卡片边框。 */
import { chromium } from 'playwright-core';
import { writeFileSync } from 'node:fs';

const BASE = 'http://localhost:8080';
const browser = await chromium.launch({
    executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',
    headless: true
});
const context = await browser.newContext({ viewport: { width: 1440, height: 900 }, locale: 'zh-CN' });
const page = await context.newPage();
const lines = [];

async function login(empNo) {
    await page.goto(`${BASE}/login.html`, { waitUntil: 'networkidle' });
    const loginCard = await page.evaluate(() => {
        const logo = document.querySelector('.login-brand img');
        const eyebrow = document.querySelector('.login-eyebrow');
        const title = document.querySelector('.login-brand h1');
        const button = document.querySelector('.login-form .btn');
        const pick = (el, props) => el ? Object.fromEntries(props.map((p) => [p, getComputedStyle(el)[p]])) : null;
        return {
            logo: logo ? { src: logo.getAttribute('src'), naturalWidth: logo.naturalWidth, width: getComputedStyle(logo).width } : null,
            eyebrow: pick(eyebrow, ['fontSize', 'fontWeight', 'letterSpacing', 'color']),
            title: pick(title, ['fontSize', 'fontWeight', 'color']),
            button: pick(button, ['height', 'fontSize', 'fontWeight', 'backgroundColor'])
        };
    });
    lines.push('LOGIN ' + JSON.stringify(loginCard));
    await page.fill('#empNo', empNo);
    await page.fill('#password', '123456');
    await page.click('#login-button');
    await page.waitForURL((url) => !url.pathname.endsWith('/login.html'), { timeout: 15000 });
}

await login('M0001');
await page.goto(`${BASE}/manager/dashboard.html`, { waitUntil: 'networkidle' });
await page.waitForTimeout(800);

const audit = await page.evaluate(() => {
    const pick = (selector, props) => {
        const el = document.querySelector(selector);
        if (!el) return null;
        const style = getComputedStyle(el);
        return Object.fromEntries(props.map((prop) => [prop, style[prop]]));
    };
    return {
        body: pick('body', ['fontFamily', 'fontSize', 'fontWeight', 'color', 'backgroundColor']),
        pageTitle: pick('.page-head h1', ['fontSize', 'fontWeight', 'lineHeight', 'color', 'letterSpacing']),
        pageDesc: pick('.page-desc', ['fontSize', 'fontWeight', 'color']),
        tableHead: pick('.data-table th', ['fontSize', 'fontWeight', 'color', 'padding']),
        tableCell: pick('.data-table td', ['fontSize', 'fontWeight', 'color', 'padding']),
        statValue: pick('.stat-value', ['fontSize', 'fontWeight', 'fontFamily', 'letterSpacing', 'fontVariantNumeric']),
        statLabel: pick('.stat-label', ['fontSize', 'fontWeight', 'color']),
        statCard: pick('.stat-grid.bento .stat-card', ['borderRadius', 'backgroundColor', 'minHeight', 'padding', 'border']),
        statIcon: pick('.stat-grid.bento .stat-icon', ['width', 'height', 'borderRadius', 'color']),
        card: pick('.card', ['border', 'borderRadius', 'boxShadow', 'backgroundColor']),
        cardBody: pick('.card-body', ['padding']),
        navItem: pick('.topnav-item.active', ['fontSize', 'fontWeight', 'color', 'backgroundColor'])
    };
});
lines.push('DASHBOARD ' + JSON.stringify(audit, null, 2));

await page.goto(`${BASE}/me/salary.html`, { waitUntil: 'networkidle' });
await page.waitForTimeout(800);
const salaryAudit = await page.evaluate(() => {
    const value = document.querySelector('.stat-value');
    const cell = document.querySelector('.data-table td.num');
    const pick = (el, props) => el ? Object.fromEntries(props.map((p) => [p, getComputedStyle(el)[p]])) : null;
    return {
        statValue: pick(value, ['fontSize', 'fontWeight', 'fontFamily', 'fontVariantNumeric']),
        tableNum: pick(cell, ['fontFamily', 'fontVariantNumeric', 'fontSize', 'fontWeight'])
    };
});
lines.push('SALARY ' + JSON.stringify(salaryAudit, null, 2));

writeFileSync('tools/design-audit.txt', lines.join('\n') + '\n', 'utf8');
console.log(lines.join('\n'));

await browser.close();
