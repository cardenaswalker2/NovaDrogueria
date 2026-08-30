import os
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, Image, PageBreak, HRFlowable
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
            self.drawString(36, 762, "TECNOLÓGICO COMFENALCO — INFORME TÉCNICO CI/CD")
            self.setFont("Helvetica", 8)
            self.setFillColor(colors.HexColor("#718096"))
            self.drawRightString(576, 762, "NovaDrogueria | Pipeline Spring Boot & GitHub Actions")
            self.setStrokeColor(colors.HexColor("#CBD5E0"))
            self.setLineWidth(0.5)
            self.line(36, 756, 576, 756)

        # Footer
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#718096"))
        self.drawString(36, 25, "Tecnológico Comfenalco | Integrantes: L. Cardenas, C. Villamil, D. Gutierrez, J. Castillo")
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

    primary = colors.HexColor("#0F294A")
    secondary = colors.HexColor("#2B6CB0")
    dark_text = colors.HexColor("#1A202C")
    light_bg = colors.HexColor("#F7FAFC")
    border_color = colors.HexColor("#E2E8F0")

    title_style = ParagraphStyle(
        'DocTitle',
        fontName='Helvetica-Bold',
        fontSize=15,
        leading=18,
        textColor=primary,
        spaceAfter=2
    )
    subtitle_style = ParagraphStyle(
        'DocSubTitle',
        fontName='Helvetica',
        fontSize=8.5,
        leading=11,
        textColor=secondary,
        spaceAfter=4
    )
    h1_style = ParagraphStyle(
        'Heading1_Custom',
        fontName='Helvetica-Bold',
        fontSize=10.5,
        leading=13,
        textColor=primary,
        spaceBefore=4,
        spaceAfter=3,
        keepWithNext=True
    )
    body_style = ParagraphStyle(
        'Body_Custom',
        fontName='Helvetica',
        fontSize=7.8,
        leading=10,
        textColor=dark_text,
        spaceAfter=3
    )
    body_bold = ParagraphStyle(
        'Body_Bold',
        fontName='Helvetica-Bold',
        fontSize=7.8,
        leading=10,
        textColor=dark_text
    )
    callout_style = ParagraphStyle(
        'Callout',
        fontName='Helvetica',
        fontSize=7.6,
        leading=9.8,
        textColor=colors.HexColor("#2C5282")
    )
    caption_style = ParagraphStyle(
        'Caption',
        fontName='Helvetica-Oblique',
        fontSize=6.8,
        leading=8.2,
        textColor=colors.HexColor("#4A5568"),
        alignment=1,
        spaceBefore=1,
        spaceAfter=3
    )

    story = []
    img_dir = "evidencias/cicd"

    # ==========================================
    # PÁGINA 1: Encabezado Institucional, Entorno y Auditoría
    # ==========================================
    story.append(Paragraph("TALLER PRÁCTICO DE CI/CD CON SPRING BOOT", title_style))
    story.append(Paragraph("<b>Institución:</b> Tecnológico Comfenalco | <b>Proyecto:</b> NovaDrogueria | <b>Fecha:</b> Agosto 2026", subtitle_style))
    story.append(Paragraph("<b>Integrantes:</b> Luis Cardenas | Cristobal Villamil | Daniel Gutierrez | Jose Castillo", subtitle_style))
    story.append(HRFlowable(width="100%", thickness=1.5, color=primary, spaceBefore=1, spaceAfter=4))

    meta_data = [
        [Paragraph("<b>Entorno & Stack</b>", body_bold), Paragraph("Java 17 (Temurin LTS), Spring Boot 3.4.2, Maven 3.9.11, MongoDB Replica Set (rs0)", body_style)],
        [Paragraph("<b>Pipeline Triggers</b>", body_bold), Paragraph("Push en <code>main</code>, <code>develop</code>, <code>chore/**</code>, <code>feature/**</code> | Pull Requests hacia <code>main</code>", body_style)],
        [Paragraph("<b>Seguridad Git</b>", body_bold), Paragraph(".gitignore protege: <code>target/</code>, <code>.env</code>, <code>application-local.properties</code>, <code>db_data/</code>", body_style)],
        [Paragraph("<b>Repositorio GitHub</b>", body_bold), Paragraph("<code>https://github.com/cardenaswalker2/NovaDrogueria.git</code>", body_style)]
    ]
    meta_table = Table(meta_data, colWidths=[110, 430])
    meta_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), light_bg),
        ('BOX', (0, 0), (-1, -1), 0.5, border_color),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, border_color),
        ('TOPPADDING', (0, 0), (-1, -1), 2.5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 2.5),
        ('LEFTPADDING', (0, 0), (-1, -1), 5),
        ('RIGHTPADDING', (0, 0), (-1, -1), 5),
    ]))
    story.append(meta_table)
    story.append(Spacer(1, 3))

    story.append(Paragraph("1. Preparación del Repositorio y Validación del Build Original", h1_style))
    story.append(Paragraph("El proyecto NovaDrogueria se auditó técnicamente manteniendo intactas todas sus funcionalidades farmacéuticas (gestión de catálogo, inventario, control de apartados y transacciones multi-documento). Se validó la compatibilidad de Java 17 y Spring Boot 3.4.2, ejecutando <code>mvn clean package</code> con resultado <b>BUILD SUCCESS</b>.", body_style))

    img1_path = os.path.join(img_dir, "01-build-local.png")
    if os.path.exists(img1_path):
        story.append(Image(img1_path, width=540, height=195))
        story.append(Paragraph("<b>Figura 1.1:</b> Reporte de cobertura y compilación local generado por Maven con JaCoCo.", caption_style))

    story.append(Paragraph("2. Auditoría de Seguridad y Gestión de Archivos Sensibles", h1_style))
    story.append(Paragraph("Se actualizó el archivo <code>.gitignore</code> para impedir la subida de variables de entorno locales (<code>.env</code>, <code>application-local.properties</code>), binarios compilados y archivos de base de datos. La aplicación utiliza inyección de credenciales mediante variables administradas con valores por defecto seguros para testing.", body_style))

    story.append(PageBreak())

    # ==========================================
    # PÁGINA 2: GitHub Actions & Pruebas Unitarias
    # ==========================================
    story.append(Paragraph("3. Pipeline Automatizado de GitHub Actions (.github/workflows/ci.yml)", h1_style))
    story.append(Paragraph("Se implementó un flujo continuo profesional con las siguientes fases: <i>Checkout &rarr; Java 17 Temurin &rarr; Contenedor MongoDB Replica Set (rs0) &rarr; Compilación Maven &rarr; JUnit 5 &rarr; JaCoCo Report &rarr; Verificación de Secreto &rarr; Upload Artifact</i>.", body_style))

    img2_path = os.path.join(img_dir, "02-actions-success.png")
    if os.path.exists(img2_path):
        story.append(Image(img2_path, width=540, height=185))
        story.append(Paragraph("<b>Figura 2.1:</b> Ejecución exitosa de GitHub Actions en la validación continua (Run ID: 33327021879).", caption_style))

    story.append(Paragraph("4. Pruebas Unitarias y de Controlador Realizadas", h1_style))
    story.append(Paragraph("Se desarrollaron 10 pruebas nuevas que se integraron a las 16 preexistentes, alcanzando un total de <b>26 pruebas automáticas reales</b>:", body_style))

    tests_summary = [
        [Paragraph("<b>Componente</b>", body_bold), Paragraph("<b>Clase de Test</b>", body_bold), Paragraph("<b>Funcionalidad Evaluada</b>", body_bold), Paragraph("<b>Resultado</b>", body_bold)],
        [Paragraph("Lógica de Servicio", body_style), Paragraph("<code>ProductServiceTest</code>", body_style), Paragraph("Creación de medicamentos, slugs únicos, precio/stock no negativos, soft delete.", body_style), Paragraph("<b>7/7 PASARON</b>", body_style)],
        [Paragraph("Controlador Web", body_style), Paragraph("<code>ProductApiControllerTest</code>", body_style), Paragraph("Búsqueda <code>/api/productos/buscar</code> con MockMvc y manejo de querys en blanco.", body_style), Paragraph("<b>2/2 PASARON</b>", body_style)],
        [Paragraph("Estado API", body_style), Paragraph("<code>StatusApiControllerTest</code>", body_style), Paragraph("Endpoint <code>/api/estado</code>, código HTTP 200 y JSON de disponibilidad.", body_style), Paragraph("<b>1/1 PASÓ</b>", body_style)],
        [Paragraph("Integración & Dominio", body_style), Paragraph("<code>ConcurrencyTest</code>, etc.", body_style), Paragraph("Concurrencia de reservas, normalización de teléfonos y formato en moneda COP.", body_style), Paragraph("<b>16/16 PASARON</b>", body_style)],
    ]
    t_table = Table(tests_summary, colWidths=[85, 125, 255, 75])
    t_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), secondary),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.white),
        ('GRID', (0, 0), (-1, -1), 0.5, border_color),
        ('TOPPADDING', (0, 0), (-1, -1), 2),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 2),
        ('LEFTPADDING', (0, 0), (-1, -1), 4),
        ('RIGHTPADDING', (0, 0), (-1, -1), 4),
    ]))
    story.append(t_table)
    story.append(Spacer(1, 2))

    img3_path = os.path.join(img_dir, "03-tests-success.png")
    if os.path.exists(img3_path):
        story.append(Image(img3_path, width=540, height=160))
        story.append(Paragraph("<b>Figura 2.2:</b> Detalle de ejecución del job 'Build, Test & JaCoCo Coverage' validando los 26 tests en verde.", caption_style))

    story.append(PageBreak())

    # ==========================================
    # PÁGINA 3: Cobertura JaCoCo & Control de Calidad
    # ==========================================
    story.append(Paragraph("5. Cobertura de Código con JaCoCo & Artifacts", h1_style))
    story.append(Paragraph("Se integró <code>jacoco-maven-plugin:0.8.12</code> en el ciclo de pruebas. La cobertura global alcanzó <b>29% de instrucciones</b> (1,772 / 6,018) y <b>37% de métodos</b> sobre 32 clases, sobresaliendo con <b>88%</b> en utilitarios (formato monetario colombiano), <b>80%</b> en seguridad y <b>37%</b> en servicios de negocio. El reporte HTML se exporta como el artefacto <code>reporte-jacoco</code>.", body_style))

    img5_path = os.path.join(img_dir, "05-jacoco-report.png")
    if os.path.exists(img5_path):
        story.append(Image(img5_path, width=540, height=170))
        story.append(Paragraph("<b>Figura 3.1:</b> Reporte detallado de cobertura JaCoCo para los paquetes de NovaDrogueria.", caption_style))

    story.append(Paragraph("6. Control de Calidad: Demostración de Detención ante Fallos y Recuperación", h1_style))
    story.append(Paragraph("Para comprobar que el pipeline actúa como barrera de control, se introdujo un fallo deliberado en un assertion (commit <code>76786d8</code>). El pipeline <b>detuvo su ejecución en ROJO</b> (Run ID: 33325679422). Tras corregir el error (commit <code>06d8293</code>), el flujo <b>retornó a VERDE</b> automáticamente.", body_style))

    img6_path = os.path.join(img_dir, "06-pipeline-failed.png")
    img7_path = os.path.join(img_dir, "07-pipeline-recovered.png")
    
    if os.path.exists(img6_path) and os.path.exists(img7_path):
        comp_data = [
            [Image(img6_path, width=265, height=160), Image(img7_path, width=265, height=160)],
            [Paragraph("<b>Figura 3.2:</b> Pipeline en ROJO (Fallo detectado)", caption_style), Paragraph("<b>Figura 3.3:</b> Pipeline en VERDE (Recuperado)", caption_style)]
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
    # PÁGINA 4: Pull Request Real, Seguridad y Reflexión
    # ==========================================
    story.append(Paragraph("7. Pull Request Real, Feature Branch y Branch Protection", h1_style))
    story.append(Paragraph("Se creó la rama <code>feature/endpoint-estado</code> y se abrió el <b>Pull Request oficial #1</b> hacia <code>main</code>. Se validaron los checks automáticos del pipeline y se aplicó la regla de protección en <code>main</code> (<i>Require status checks to pass before merging</i>), culminando con la fusión formal (*Merged*).", body_style))

    img10_path = os.path.join(img_dir, "10-pull-request.png")
    if os.path.exists(img10_path):
        story.append(Image(img10_path, width=540, height=160))
        story.append(Paragraph("<b>Figura 4.1:</b> Pull Request oficial #1 en GitHub con checks de CI/CD aprobados y estado Merged.", caption_style))

    story.append(Paragraph("8. Gestión de Secretos en GitHub Actions", h1_style))
    story.append(Paragraph("Se configuró el secreto de entorno <code>APP_ENV_DEMO</code> en el repositorio. El pipeline lo consume vía <code>${{ secrets.APP_ENV_DEMO }}</code> realizando una verificación enmascarada con huella SHA-256 en consola, sin exponer nunca el valor en texto plano.", body_style))

    story.append(Paragraph("9. Reflexión Técnica del Equipo de Desarrollo", h1_style))
    reflexion_text = (
        "<b>Reflexión del Proyecto:</b> El desafío técnico más relevante fue resolver la dependencia transaccional de MongoDB. "
        "El servicio <code>ReservationService</code> requiere soporte transaccional ACID mediante <code>MongoTransactionManager</code>, "
        "lo que exige un Replica Set. En instancias independientes, los tests fallaban con <code>UncategorizedMongoDbException</code>. "
        "En lugar de alterar el diseño del software o suprimir las pruebas, se configuró en GitHub Actions un contenedor Docker "
        "de MongoDB 7.0 inicializado con <code>rs0</code> vía <code>mongosh</code>. "
        "Esta solución preservó la arquitectura original y evidenció la importancia de que los pipelines de integración continua "
        "repliquen fielmente los requerimientos de la infraestructura de producción."
    )
    
    ref_table = Table([[Paragraph(reflexion_text, callout_style)]], colWidths=[540])
    ref_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor("#EBF8FF")),
        ('BOX', (0, 0), (-1, -1), 1, colors.HexColor("#3182CE")),
        ('LEFTPADDING', (0, 0), (-1, -1), 7),
        ('RIGHTPADDING', (0, 0), (-1, -1), 7),
        ('TOPPADDING', (0, 0), (-1, -1), 5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
    ]))
    story.append(ref_table)

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"PDF successfully regenerated: {filename}")

if __name__ == "__main__":
    build_pdf()
