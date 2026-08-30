import os
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, Image, KeepTogether, PageBreak, HRFlowable
)
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_header_footer(num_pages)
            super().showPage()
        super().save()

    def draw_header_footer(self, page_count):
        self.saveState()
        self.setFont("Helvetica-Bold", 8)
        self.setFillColor(colors.HexColor("#1A365D"))
        
        # Header (pages 2+)
        if self._pageNumber > 1:
            self.drawString(36, 762, "INFORME TÉCNICO CI/CD — PROYECTO NOVADROGUERIA")
            self.setFont("Helvetica", 8)
            self.setFillColor(colors.HexColor("#718096"))
            self.drawRightString(576, 762, "Spring Boot & GitHub Actions Pipeline")
            self.setStrokeColor(colors.HexColor("#CBD5E0"))
            self.setLineWidth(0.5)
            self.line(36, 756, 576, 756)

        # Footer
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#718096"))
        self.drawString(36, 25, "Universidad / Taller CI/CD Profesional — Cárdenas Walker")
        page_text = f"Página {self._pageNumber} de {page_count}"
        self.drawRightString(576, 25, page_text)
        self.setStrokeColor(colors.HexColor("#CBD5E0"))
        self.setLineWidth(0.5)
        self.line(36, 35, 576, 35)
        self.restoreState()

def build_pdf(filename="taller-cicd-springboot_cardenas-walker.pdf"):
    doc = SimpleDocTemplate(
        filename,
        pagesize=letter,
        leftMargin=36,
        rightMargin=36,
        topMargin=36,
        bottomMargin=45
    )

    styles = getSampleStyleSheet()
    
    # Custom Palette
    primary = colors.HexColor("#0F294A")
    secondary = colors.HexColor("#2B6CB0")
    accent = colors.HexColor("#319795")
    dark_text = colors.HexColor("#1A202C")
    light_bg = colors.HexColor("#F7FAFC")
    border_color = colors.HexColor("#E2E8F0")

    # Typography Styles
    title_style = ParagraphStyle(
        'DocTitle',
        fontName='Helvetica-Bold',
        fontSize=17,
        leading=20,
        textColor=primary,
        spaceAfter=3
    )
    subtitle_style = ParagraphStyle(
        'DocSubTitle',
        fontName='Helvetica',
        fontSize=9.5,
        leading=12,
        textColor=secondary,
        spaceAfter=6
    )
    h1_style = ParagraphStyle(
        'Heading1_Custom',
        fontName='Helvetica-Bold',
        fontSize=11.5,
        leading=14,
        textColor=primary,
        spaceBefore=5,
        spaceAfter=4,
        keepWithNext=True
    )
    h2_style = ParagraphStyle(
        'Heading2_Custom',
        fontName='Helvetica-Bold',
        fontSize=9.5,
        leading=12,
        textColor=secondary,
        spaceBefore=4,
        spaceAfter=3,
        keepWithNext=True
    )
    body_style = ParagraphStyle(
        'Body_Custom',
        fontName='Helvetica',
        fontSize=8,
        leading=10.5,
        textColor=dark_text,
        spaceAfter=3
    )
    body_bold = ParagraphStyle(
        'Body_Bold',
        fontName='Helvetica-Bold',
        fontSize=8,
        leading=10.5,
        textColor=dark_text
    )
    callout_style = ParagraphStyle(
        'Callout',
        fontName='Helvetica-Oblique',
        fontSize=7.8,
        leading=10,
        textColor=colors.HexColor("#2C5282")
    )
    caption_style = ParagraphStyle(
        'Caption',
        fontName='Helvetica-Oblique',
        fontSize=7,
        leading=8.5,
        textColor=colors.HexColor("#4A5568"),
        alignment=1,
        spaceBefore=2,
        spaceAfter=4
    )

    story = []
    img_dir = "evidencias/cicd"

    # ==========================================
    # PÁGINA 1: Encabezado, Entorno y Auditoría
    # ==========================================
    story.append(Paragraph("TALLER PRÁCTICO DE CI/CD CON SPRING BOOT", title_style))
    story.append(Paragraph("<b>Proyecto:</b> NovaDrogueria | <b>Especialista:</b> Cárdenas Walker | <b>Fecha:</b> Agosto 2026 | <b>Repositorio:</b> cardenaswalker2/NovaDrogueria", subtitle_style))
    story.append(HRFlowable(width="100%", thickness=1.5, color=primary, spaceBefore=1, spaceAfter=5))

    # Meta Table
    meta_data = [
        [Paragraph("<b>Entorno & Stack</b>", body_bold), Paragraph("Java 17 (Temurin LTS), Spring Boot 3.4.2, Maven 3.9.11, MongoDB Replica Set (rs0)", body_style)],
        [Paragraph("<b>Pipeline Triggers</b>", body_bold), Paragraph("Push en <code>main</code>, <code>develop</code>, <code>chore/**</code>, <code>feature/**</code> | Pull Requests hacia <code>main</code>", body_style)],
        [Paragraph("<b>Seguridad Git</b>", body_bold), Paragraph(".gitignore protege: <code>target/</code>, <code>.env</code>, <code>application-local.properties</code>, <code>db_data/</code>", body_style)]
    ]
    meta_table = Table(meta_data, colWidths=[110, 430])
    meta_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), light_bg),
        ('BOX', (0, 0), (-1, -1), 0.5, border_color),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, border_color),
        ('TOPPADDING', (0, 0), (-1, -1), 3),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 3),
        ('LEFTPADDING', (0, 0), (-1, -1), 5),
        ('RIGHTPADDING', (0, 0), (-1, -1), 5),
    ]))
    story.append(meta_table)
    story.append(Spacer(1, 4))

    story.append(Paragraph("1. Preparación del Repositorio y Validación del Build Original", h1_style))
    story.append(Paragraph("El proyecto NovaDrogueria se auditó integralmente sin alterar sus funcionalidades farmacéuticas (gestión de medicamentos, stock, apartados y transacciones). Se verificó la compatibilidad de Java 17 y Spring Boot 3.4.2, ejecutando <code>mvn clean package</code> con resultado <b>BUILD SUCCESS</b>.", body_style))

    # Imagen Build Local / Reporte
    img1_path = os.path.join(img_dir, "01-build-local.png")
    if os.path.exists(img1_path):
        story.append(Image(img1_path, width=540, height=210))
        story.append(Paragraph("<b>Figura 1.1:</b> Reporte de compilación y cobertura de ejecución local generado por Maven.", caption_style))

    story.append(Paragraph("2. Auditoría de Seguridad y Gestión de Archivos Sensibles", h1_style))
    story.append(Paragraph("Se blindó el archivo <code>.gitignore</code> para impedir la exposición de credenciales locales, cadenas de conexión directas y artefactos compilados. La infraestructura utiliza variables de entorno administradas (<code>ADMIN_USERNAME</code>, <code>ADMIN_PASSWORD</code>) con valores por defecto seguros para testing y contenedor.", body_style))

    story.append(PageBreak())

    # ==========================================
    # PÁGINA 2: GitHub Actions & Pruebas Unitarias
    # ==========================================
    story.append(Paragraph("3. Pipeline Automatizado de GitHub Actions (.github/workflows/ci.yml)", h1_style))
    story.append(Paragraph("Se implementó un workflow profesional que orquesta: <i>Checkout &rarr; Setup Java 17 Temurin &rarr; Contenedor MongoDB Replica Set (rs0) &rarr; Compilación Maven &rarr; JUnit 5 &rarr; JaCoCo Report &rarr; Verificación de Secreto &rarr; Publicación de Artifacts</i>.", body_style))

    img2_path = os.path.join(img_dir, "02-actions-success.png")
    if os.path.exists(img2_path):
        story.append(Image(img2_path, width=540, height=195))
        story.append(Paragraph("<b>Figura 2.1:</b> Ejecución exitosa (Run ID: 33325603850) del pipeline CI/CD en GitHub Actions.", caption_style))

    story.append(Paragraph("4. Pruebas Unitarias y de Controlador Realizadas", h1_style))
    story.append(Paragraph("Se diseñaron e integraron pruebas automáticas de alta fidelidad sin recurrir a datos ficticios:", body_style))

    tests_summary = [
        [Paragraph("<b>Componente</b>", body_bold), Paragraph("<b>Clase de Test</b>", body_bold), Paragraph("<b>Aspectos Validados</b>", body_bold), Paragraph("<b>Resultado</b>", body_bold)],
        [Paragraph("Lógica de Servicio", body_style), Paragraph("<code>ProductServiceTest</code>", body_style), Paragraph("Creación de productos, validación de slugs duplicados, precios/stock negativos, soft delete.", body_style), Paragraph("<b>PASÓ (7/7)</b>", body_style)],
        [Paragraph("Controlador Web", body_style), Paragraph("<code>ProductApiControllerTest</code>", body_style), Paragraph("Búsqueda reactiva <code>/api/productos/buscar</code> con MockMvc, filtros y sanitización.", body_style), Paragraph("<b>PASÓ (2/2)</b>", body_style)],
        [Paragraph("Estado API", body_style), Paragraph("<code>StatusApiControllerTest</code>", body_style), Paragraph("Endpoint <code>/api/estado</code>, headers HTTP 200, payload JSON de salud del sistema.", body_style), Paragraph("<b>PASÓ (1/1)</b>", body_style)],
        [Paragraph("Integración & Stock", body_style), Paragraph("<code>ConcurrencyTest</code>, etc.", body_style), Paragraph("Concurrencia de inventario, normalización telefónica y flujo transaccional de apartados.", body_style), Paragraph("<b>PASÓ (16/16)</b>", body_style)],
    ]
    t_table = Table(tests_summary, colWidths=[85, 125, 255, 75])
    t_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), secondary),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.white),
        ('GRID', (0, 0), (-1, -1), 0.5, border_color),
        ('TOPPADDING', (0, 0), (-1, -1), 2.5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 2.5),
        ('LEFTPADDING', (0, 0), (-1, -1), 4),
        ('RIGHTPADDING', (0, 0), (-1, -1), 4),
    ]))
    story.append(t_table)
    story.append(Spacer(1, 3))

    img3_path = os.path.join(img_dir, "03-tests-success.png")
    if os.path.exists(img3_path):
        story.append(Image(img3_path, width=540, height=170))
        story.append(Paragraph("<b>Figura 2.2:</b> Validación de 26 pruebas automáticas exitosas en el runner de GitHub Actions.", caption_style))

    story.append(PageBreak())

    # ==========================================
    # PÁGINA 3: Cobertura JaCoCo & Demostración de Fallo
    # ==========================================
    story.append(Paragraph("5. Cobertura de Código con JaCoCo & Artifacts", h1_style))
    story.append(Paragraph("Se integró <code>jacoco-maven-plugin:0.8.12</code> vinculado a la fase <code>test</code>. La cobertura global alcanzó <b>29% de instrucciones</b> y <b>37% de métodos</b> sobre 32 clases, destacando <b>88%</b> en utilitarios y <b>80%</b> en seguridad. El reporte HTML se empaqueta y publica automáticamente como el artefacto <code>reporte-jacoco</code>.", body_style))

    img5_path = os.path.join(img_dir, "05-jacoco-report.png")
    if os.path.exists(img5_path):
        story.append(Image(img5_path, width=540, height=175))
        story.append(Paragraph("<b>Figura 3.1:</b> Reporte detallado de cobertura JaCoCo generado para el paquete NovaDrogueria.", caption_style))

    story.append(Paragraph("6. Demostración de Detención del Pipeline ante Fallos (Control de Calidad)", h1_style))
    story.append(Paragraph("Para comprobar que el CI actúa como guardián estricto, se introdujo un fallo deliberado en un assertion (commit <code>76786d8</code>). GitHub Actions <b>detuvo inmediatamente el pipeline en ROJO</b> (Run ID 33325679422). Al revertir el fallo (commit <code>06d8293</code>), el pipeline <b>retornó a VERDE</b> automáticamente.", body_style))

    # Two images side by side
    img6_path = os.path.join(img_dir, "06-pipeline-failed.png")
    img7_path = os.path.join(img_dir, "07-pipeline-recovered.png")
    
    comp_images = []
    if os.path.exists(img6_path) and os.path.exists(img7_path):
        comp_data = [
            [Image(img6_path, width=265, height=165), Image(img7_path, width=265, height=165)],
            [Paragraph("<b>Figura 3.2:</b> Pipeline ROJO (Fallo detectado)", caption_style), Paragraph("<b>Figura 3.3:</b> Pipeline VERDE (Recuperado)", caption_style)]
        ]
        comp_table = Table(comp_data, colWidths=[270, 270])
        comp_table.setStyle(TableStyle([
            ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
            ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
            ('LEFTPADDING', (0, 0), (-1, -1), 0),
            ('RIGHTPADDING', (0, 0), (-1, -1), 0),
            ('TOPPADDING', (0, 0), (-1, -1), 0),
            ('BOTTOMPADDING', (0, 0), (-1, -1), 0),
        ]))
        story.append(comp_table)

    story.append(PageBreak())

    # ==========================================
    # PÁGINA 4: Pull Request, Seguridad y Reflexión
    # ==========================================
    story.append(Paragraph("7. Pull Request Real, Feature Branch y Branch Protection", h1_style))
    story.append(Paragraph("Se desarrolló la rama <code>feature/endpoint-estado</code> agregando el endpoint <code>GET /api/estado</code>. Se configuró la regla de protección de rama en <code>main</code> (<i>Require status checks to pass before merging</i>) garantizando que ningún código entre a producción sin pasar el pipeline.", body_style))

    img10_path = os.path.join(img_dir, "10-pull-request.png")
    if os.path.exists(img10_path):
        story.append(Image(img10_path, width=540, height=170))
        story.append(Paragraph("<b>Figura 4.1:</b> Comparación y Pull Request de <code>feature/endpoint-estado</code> hacia <code>main</code> con validación CI.", caption_style))

    story.append(Paragraph("8. Gestión de Secretos en GitHub Actions", h1_style))
    story.append(Paragraph("Se configuró el secreto de entorno <code>APP_ENV_DEMO</code> (valor de prueba: <code>workshop-2026</code>). El pipeline lo consume vía <code>${{ secrets.APP_ENV_DEMO }}</code> realizando verificaciones de longitud y fingerprint SHA-256 enmascarado, sin filtrar el texto plano en los registros.", body_style))

    story.append(Paragraph("9. Reflexión Técnica del Ingeniero (Caso Real)", h1_style))
    reflexion_text = (
        "<b>Reflexión Profesional:</b> El mayor desafío técnico enfrentado durante la implementación fue la dependencia transaccional de MongoDB. "
        "El servicio de reservas <code>ReservationService</code> utiliza anotaciones <code>@Transactional</code> respaldadas por <code>MongoTransactionManager</code>, "
        "lo cual exige obligatoriamente un conjunto de réplicas (<i>Replica Set</i>). Al ejecutar los tests en entornos standalone locales y en los runners de GitHub Actions, "
        "las pruebas arrojaban <code>UncategorizedMongoDbException: retryable writes not supported</code>. "
        "Tras investigar la causa raíz en los logs de Surefire, en lugar de desactivar las transacciones o alterar el código de la aplicación de droguería, "
        "diseñé un contenedor Docker de MongoDB 7.0 en el workflow que inicializa un Replica Set <code>rs0</code> al vuelo mediante <code>mongosh --eval 'rs.initiate()'</code>. "
        "Esta solución mantuvo 100% intacta la integridad transaccional de producción y demostró cómo la infraestructura como código en CI debe replicar fielmente los requerimientos del software."
    )
    
    ref_table = Table([[Paragraph(reflexion_text, callout_style)]], colWidths=[540])
    ref_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor("#EBF8FF")),
        ('BOX', (0, 0), (-1, -1), 1, colors.HexColor("#3182CE")),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
        ('TOPPADDING', (0, 0), (-1, -1), 6),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 6),
    ]))
    story.append(ref_table)

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"PDF successfully generated: {filename}")

if __name__ == "__main__":
    build_pdf()
