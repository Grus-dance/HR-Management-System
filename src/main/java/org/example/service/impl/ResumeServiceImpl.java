package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.example.common.BusinessException;
import org.example.entity.Employee;
import org.example.entity.WorkExperience;
import org.example.mapper.EmployeeMapper;
import org.example.service.ResumeService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF 简历服务实现：使用 PDFBox + 系统中文字体渲染。
 */
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final EmployeeMapper employeeMapper;

    @Override
    public byte[] buildPdf(Long employeeId) {
        Employee employee = employeeMapper.findById(employeeId);
        if (employee == null) {
            throw BusinessException.notFound("员工不存在");
        }
        List<WorkExperience> experiences = employeeMapper.selectExperiences(employeeId);
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDFont font = loadFont(document);
            ResumeRenderer renderer = new ResumeRenderer(document, font, employee, experiences);
            renderer.render();
            document.save(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new BusinessException(50001, "简历生成失败，请稍后重试");
        }
    }

    @Override
    public String fileName(Long employeeId) {
        Employee employee = employeeMapper.findById(employeeId);
        if (employee == null) {
            throw BusinessException.notFound("员工不存在");
        }
        return "简历_" + employee.getEmpNo() + "_" + employee.getName() + ".pdf";
    }

    private PDFont loadFont(PDDocument document) throws IOException {
        String[] candidates = {
                "C:/Windows/Fonts/simhei.ttf",
                "C:/Windows/Fonts/Deng.ttf",
                "C:/Windows/Fonts/simkai.ttf",
                "C:/Windows/Fonts/simfang.ttf",
                "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc",
                "/System/Library/Fonts/STHeiti Light.ttc"
        };
        for (String path : candidates) {
            File file = new File(path);
            if (file.isFile()) {
                try {
                    return PDType0Font.load(document, file);
                } catch (IOException ignored) {
                    // 尝试下一个候选字体
                }
            }
        }
        ClassPathResource resource = new ClassPathResource("fonts/resume.ttf");
        if (resource.exists()) {
            try (InputStream in = resource.getInputStream()) {
                return PDType0Font.load(document, in);
            } catch (IOException ignored) {
                // 落到统一错误提示
            }
        }
        throw new BusinessException(50001, "服务器未找到可用的中文字体，无法生成 PDF 简历");
    }

    /**
     * 单页/多页简历渲染器。
     */
    private static final class ResumeRenderer {

        private static final float LEFT = 56f;
        private static final float RIGHT = 539f;
        private static final float TOP = 786f;
        private static final float BOTTOM = 62f;
        private static final float CONTENT_WIDTH = RIGHT - LEFT;

        private final PDDocument document;
        private final PDFont font;
        private final Employee employee;
        private final List<WorkExperience> experiences;
        private PDPageContentStream stream;
        private float y = TOP;

        ResumeRenderer(PDDocument document, PDFont font, Employee employee, List<WorkExperience> experiences)
                throws IOException {
            this.document = document;
            this.font = font;
            this.employee = employee;
            this.experiences = experiences;
            newPage();
        }

        void render() throws IOException {
            color(0.10f, 0.15f, 0.25f);
            title(employee.getName() + " 的个人简历", 22);
            y -= 8;
            color(0.45f, 0.50f, 0.58f);
            centered(employee.getDepartment() + " · " + employee.getPosition(), 11);
            y -= 26;
            color(0.10f, 0.15f, 0.25f);

            section("基本信息");
            kv("员工编号", employee.getEmpNo(), "性别", "M".equals(employee.getGender()) ? "男" : "女");
            kv("出生日期", text(employee.getBirthDate()), "入职日期", text(employee.getHireDate()));
            kv("手机号", safe(employee.getPhone()), "邮箱", safe(employee.getEmail()));
            kv("学历", safe(employee.getEducation()), "专业", safe(employee.getMajor()));
            kv("毕业院校", safe(employee.getSchool()), "部门", safe(employee.getDepartment()));
            kv("职位", safe(employee.getPosition()), "紧急联系人", safe(employee.getEmergencyContact()));
            paragraph("现居地址", safe(employee.getAddress()));

            section("教育背景");
            paragraph(null, safe(employee.getSchool()) + " · " + safe(employee.getMajor()) + " · " + safe(employee.getEducation()));

            section("工作经历");
            if (experiences.isEmpty()) {
                paragraph(null, "暂无曾就职记录");
            } else {
                for (WorkExperience item : experiences) {
                    experience(item);
                }
            }

            section("在职信息");
            kv("所属部门", safe(employee.getDepartment()), "当前职位", safe(employee.getPosition()));
            kv("入职日期", text(employee.getHireDate()), "在职状态", "ACTIVE".equals(employee.getStatus()) ? "在职" : "离职");

            footer();
            stream.close();
        }

        private void newPage() throws IOException {
            if (stream != null) {
                stream.close();
            }
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = TOP;
        }

        private void ensure(float height) throws IOException {
            if (y - height < BOTTOM) {
                newPage();
            }
        }

        private void title(String value, float size) throws IOException {
            ensure(size + 10);
            text(centerX(value, size), y, size, value);
            y -= size + 6;
        }

        private void centered(String value, float size) throws IOException {
            ensure(size + 8);
            text(centerX(value, size), y, size, value);
            y -= size + 8;
        }

        private void section(String title) throws IOException {
            ensure(46);
            y -= 10;
            color(0.15f, 0.32f, 0.72f);
            text(LEFT, y, 13, title);
            y -= 7;
            stream.setStrokingColor(0.85f, 0.88f, 0.94f);
            stream.setLineWidth(0.8f);
            stream.moveTo(LEFT, y);
            stream.lineTo(RIGHT, y);
            stream.stroke();
            y -= 18;
            color(0.10f, 0.15f, 0.25f);
        }

        private void kv(String label1, String value1, String label2, String value2) throws IOException {
            ensure(20);
            text(LEFT, y, 9.5f, label1 + "：");
            text(LEFT + 56, y, 10.5f, value1);
            text(LEFT + 250, y, 9.5f, label2 + "：");
            text(LEFT + 306, y, 10.5f, value2);
            y -= 20;
        }

        private void paragraph(String label, String value) throws IOException {
            if (label != null) {
                ensure(20);
                text(LEFT, y, 9.5f, label + "：");
                text(LEFT + 56, y, 10.5f, value);
                y -= 20;
                return;
            }
            for (String line : wrap(value, 10.5f, CONTENT_WIDTH)) {
                ensure(18);
                text(LEFT, y, 10.5f, line);
                y -= 18;
            }
            y -= 2;
        }

        private void experience(WorkExperience item) throws IOException {
            ensure(52);
            text(LEFT, y, 11.5f, safe(item.getCompany()) + " · " + safe(item.getPosition()));
            String range = text(item.getStartDate()) + " 至 " + text(item.getEndDate());
            text(RIGHT - width(range, 9.5f), y, 9.5f, range);
            y -= 18;
            color(0.35f, 0.40f, 0.48f);
            for (String line : wrap(safe(item.getDescription()), 10f, CONTENT_WIDTH - 10)) {
                ensure(16);
                text(LEFT + 10, y, 10f, line);
                y -= 16;
            }
            color(0.10f, 0.15f, 0.25f);
            y -= 6;
        }

        private void footer() throws IOException {
            String footer = "X 公司人事管理系统 生成 · " + LocalDate.now().format(DATE);
            color(0.62f, 0.66f, 0.72f);
            text(centerX(footer, 8.5f), BOTTOM - 18, 8.5f, footer);
        }

        private void text(float x, float y, float size, String value) throws IOException {
            stream.beginText();
            stream.setFont(font, size);
            stream.newLineAtOffset(x, y);
            stream.showText(value);
            stream.endText();
        }

        private float width(String value, float size) {
            try {
                return font.getStringWidth(value) / 1000f * size;
            } catch (IOException ex) {
                return value.length() * size * 0.6f;
            }
        }

        private float centerX(String value, float size) {
            return LEFT + (CONTENT_WIDTH - width(value, size)) / 2;
        }

        private List<String> wrap(String value, float size, float maxWidth) {
            List<String> lines = new ArrayList<>();
            if (value == null || value.isBlank()) {
                return lines;
            }
            StringBuilder line = new StringBuilder();
            float used = 0f;
            for (int i = 0; i < value.length(); ) {
                int codePoint = value.codePointAt(i);
                String ch = new String(Character.toChars(codePoint));
                i += Character.charCount(codePoint);
                float w = width(ch, size);
                if (used + w > maxWidth && line.length() > 0) {
                    lines.add(line.toString());
                    line.setLength(0);
                    used = 0f;
                }
                line.append(ch);
                used += w;
            }
            if (line.length() > 0) {
                lines.add(line.toString());
            }
            return lines;
        }

        private void color(float r, float g, float b) throws IOException {
            stream.setNonStrokingColor(r, g, b);
        }

        private String safe(String value) {
            return value == null || value.isBlank() ? "—" : value;
        }

        private String text(java.time.LocalDate value) {
            return value == null ? "—" : value.format(DATE);
        }
    }
}
