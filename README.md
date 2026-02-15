
https://github.com/user-attachments/assets/33433b0b-63a2-414e-af17-4691435cb775
# GeometryForge

**Um motor de renderização 3D em tempo real construído do zero utilizando Java e OpenGL**

https://github.com/user-attachments/assets/6d5d5ed4-0ea5-483d-81d8-a0d3095625ee

---

## Índice

1. [Visão Geral](#visão-geral)
2. [Fundamentos Matemáticos](#fundamentos-matemáticos)
3. [Arquitetura do Sistema](#arquitetura-do-sistema)
4. [Pipeline de Renderização](#pipeline-de-renderização)
5. [Algoritmos Implementados](#algoritmos-implementados)
6. [Modelo de Iluminação](#modelo-de-iluminação)
7. [Sistema de Câmera](#sistema-de-câmera)
8. [Otimizações de Performance](#otimizações-de-performance)
9. [Instruções de Execução](#instruções-de-execução)
10. [Especificações Técnicas](#especificações-técnicas)

---

## Visão Geral

GeometryForge é uma implementação educacional de um motor gráfico 3D que demonstra os fundamentos da computação gráfica moderna, desde a álgebra linear até otimizações de GPU. O projeto foi desenvolvido sem dependências de engines comerciais, utilizando apenas bindings de baixo nível para OpenGL.

### Características Principais

- **Renderização em Tempo Real**: Pipeline gráfico completo com 60 FPS utilizando OpenGL 3.3 Core Profile
- **Geração Procedural de Geometria**: Implementação do algoritmo de subdivisão de icosaedro com cache de vértices
- **Modelo de Iluminação Phong**: Cálculo per-fragment com componentes ambiente, difuso e especular
- **Sistema de Câmera Orbital**: Implementação de coordenadas esféricas com controle interativo
- **Arquitetura Modular**: Separação de responsabilidades seguindo princípios SOLID

---

## Fundamentos Matemáticos

### Pipeline de Transformação MVP

O sistema implementa a cadeia completa de transformações do espaço local até o espaço de tela:

$$
\mathbf{P}_{\text{clip}} = \mathbf{M}_{\text{projection}} \cdot \mathbf{M}_{\text{view}} \cdot \mathbf{M}_{\text{model}} \cdot \mathbf{P}_{\text{local}}
$$

Onde:
- $\mathbf{P}_{\text{local}} \in \mathbb{R}^4$ — Posição do vértice em coordenadas homogêneas locais
- $\mathbf{M}_{\text{model}} \in \mathbb{R}^{4 \times 4}$ — Matriz de transformação do objeto (TRS: Translation, Rotation, Scale)
- $\mathbf{M}_{\text{view}} \in \mathbb{R}^{4 \times 4}$ — Matriz de visualização (câmera)
- $\mathbf{M}_{\text{projection}} \in \mathbb{R}^{4 \times 4}$ — Matriz de projeção perspectiva
- $\mathbf{P}_{\text{clip}} \in \mathbb{R}^4$ — Posição final em clip space

### Matriz de Projeção Perspectiva

A transformação perspectiva é definida por:

$$
\mathbf{M}_{\text{perspective}} = \begin{bmatrix}
\frac{1}{\text{aspect} \cdot \tan(\text{fov}/2)} & 0 & 0 & 0 \\
0 & \frac{1}{\tan(\text{fov}/2)} & 0 & 0 \\
0 & 0 & -\frac{f + n}{f - n} & -\frac{2fn}{f - n} \\
0 & 0 & -1 & 0
\end{bmatrix}
$$

Parâmetros:
- $\text{fov}$ — Campo de visão (45° implementado)
- $\text{aspect}$ — Razão de aspecto (largura/altura)
- $n$ — Plano próximo (near plane = 0.1)
- $f$ — Plano distante (far plane = 100.0)

### Matriz Look-At (Câmera)

Construção da base ortonormal da câmera:

$$
\begin{aligned}
\vec{f} &= \text{normalize}(\vec{e} - \vec{t}) && \text{(forward)} \\
\vec{r} &= \text{normalize}(\vec{u}_{\text{world}} \times \vec{f}) && \text{(right)} \\
\vec{u} &= \vec{f} \times \vec{r} && \text{(up)}
\end{aligned}
$$

Matriz de visualização resultante:

$$
\mathbf{M}_{\text{view}} = \begin{bmatrix}
r_x & r_y & r_z & -\vec{r} \cdot \vec{e} \\
u_x & u_y & u_z & -\vec{u} \cdot \vec{e} \\
-f_x & -f_y & -f_z & \vec{f} \cdot \vec{e} \\
0 & 0 & 0 & 1
\end{bmatrix}
$$

### Matriz Normal (Transformação de Normais)

**Problema Fundamental**: Normais não podem ser transformadas diretamente pela matriz model sob escalas não-uniformes.

**Solução Matemática**: Utiliza-se a transposta da inversa da matriz model:

$$
\mathbf{N}_{\text{world}} = (\mathbf{M}_{\text{model}}^{-1})^T \cdot \mathbf{N}_{\text{local}}
$$

**Demonstração**:

Dado um vetor tangente $\mathbf{T}$ e normal $\mathbf{N}$ onde $\mathbf{N} \cdot \mathbf{T} = 0$:

$$
\begin{aligned}
(\mathbf{M} \cdot \mathbf{T}) \cdot ((\mathbf{M}^{-1})^T \cdot \mathbf{N}) &= (\mathbf{M} \cdot \mathbf{T})^T \cdot (\mathbf{M}^{-1})^T \cdot \mathbf{N} \\
&= \mathbf{T}^T \cdot \mathbf{M}^T \cdot \mathbf{M}^{-T} \cdot \mathbf{N} \\
&= \mathbf{T}^T \cdot \mathbf{I} \cdot \mathbf{N} \\
&= \mathbf{T}^T \cdot \mathbf{N} = 0 \quad \checkmark
\end{aligned}
$$

### Coordenadas Homogêneas

Sistema de 4 componentes para representar pontos e vetores em espaço projetivo:

$$
\mathbf{p} = \begin{pmatrix} x \\ y \\ z \\ w \end{pmatrix}
$$

**Convenções**:
- Pontos: $w = 1$ (afetados por translação)
- Vetores: $w = 0$ (invariantes à translação)

**Divisão Perspectiva** (realizada automaticamente pelo rasterizador):

$$
\mathbf{p}_{\text{NDC}} = \begin{pmatrix} x/w \\ y/w \\ z/w \end{pmatrix} \in [-1, 1]^3
$$

---

## Arquitetura do Sistema

### Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────┐
│                     Main (Entry Point)                  │
│  • Gerenciamento do ciclo de vida da aplicação         │
│  • Coordenação de subsistemas                           │
└────────────┬────────────────────────────────────────────┘
             │
    ┌────────┴────────┬─────────────┬──────────────┐
    │                 │             │              │
┌───▼────┐    ┌──────▼──────┐  ┌──▼─────┐  ┌─────▼──────┐
│ Window │    │ShaderProgram│  │ Camera │  │  Geometry  │
│        │    │             │  │        │  │  Subsystem │
├────────┤    ├─────────────┤  ├────────┤  ├────────────┤
│• GLFW  │    │• Compilação │  │• Coords│  │• Mesh      │
│• Input │    │• Uniforms   │  │  Esfer.│  │• Cube      │
│• Loop  │    │• Binding    │  │• Orbit │  │• Icosphere │
└────────┘    └─────────────┘  └────────┘  └────────────┘
```

### Princípios Arquiteturais Aplicados

#### 1. Separation of Concerns (SoC)

**Window**: Responsável exclusivamente por:
- Gerenciamento do contexto GLFW
- Captura de eventos de entrada
- Loop de renderização

**ShaderProgram**: Encapsula:
- Compilação e linking de shaders
- Gerenciamento de uniforms
- Ciclo de vida dos programas GPU

**Camera**: Abstrai:
- Sistema de coordenadas esféricas
- Transformações view
- Lógica de input para movimentação

#### 2. Single Responsibility Principle (SRP)

Cada classe tem uma única razão para mudar:

```java
// Mesh.java - Responsável APENAS por geometria GPU
public class Mesh {
    private final int vaoId;    // Vertex Array Object
    private final int vboId;    // Vertex Buffer Object
    private final int nboId;    // Normal Buffer Object
    private final int eboId;    // Element Buffer Object
    
    public void render() { /* Draw call */ }
    public void cleanup() { /* Resource deallocation */ }
}
```

#### 3. Dependency Injection

```java
// Main.java - Injeção de callbacks
window.setRenderCallback(this::render);
window.setMouseMoveCallback(this::onMouseMove);
window.setMouseScrollCallback(this::onMouseScroll);
```

#### 4. Factory Pattern

```java
// Geometrias são criadas através de métodos factory
Mesh cubeMesh = Cube.createMesh();
Mesh sphereMesh = Icosphere.createMesh(subdivisions);
```

### Gerenciamento de Recursos GPU

**Padrão RAII** (Resource Acquisition Is Initialization):

```java
// Aquisição no construtor
public Mesh(float[] vertices, float[] normals, int[] indices) {
    vaoId = glGenVertexArrays();
    vboId = glGenBuffers();
    // ... alocação de recursos
}

// Liberação explícita no cleanup
public void cleanup() {
    glDeleteBuffers(vboId);
    glDeleteBuffers(nboId);
    glDeleteBuffers(eboId);
    glDeleteVertexArrays(vaoId);
}
```

---

## Pipeline de Renderização

### Fluxo Completo de Dados

```
CPU Side                              GPU Side
────────                              ────────

[Vertex Data]                    [Vertex Shader]
     │                                  │
     │ glBufferData()                   │ MVP Transform
     ▼                                  │ Normal Transform
[VBO/EBO/VAO]  ─────────────────►     │
                                       ▼
                               [Primitive Assembly]
                                       │
                                       ▼
                                 [Rasterization]
                                       │
                                       ▼ (interpolation)
                              [Fragment Shader]
                                       │
                                       │ Phong Lighting
                                       │ Color Calculation
                                       ▼
                               [Per-Fragment Tests]
                                       │
                                       │ Depth Test
                                       │ Stencil Test
                                       ▼
                                 [Framebuffer]
```

### Vertex Shader (GLSL 330)

```glsl
#version 330 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec3 fragNormal;
out vec3 fragPosition;

void main()
{
    // Transformação para world space
    vec4 worldPos = model * vec4(position, 1.0);
    fragPosition = worldPos.xyz;
    
    // Transformação de normal (matriz normal)
    fragNormal = mat3(transpose(inverse(model))) * normal;
    
    // Transformação MVP completa
    gl_Position = projection * view * worldPos;
}
```

### Fragment Shader (Phong Lighting)

```glsl
#version 330 core

in vec3 fragNormal;
in vec3 fragPosition;

out vec4 fragColor;

uniform vec3 objectColor;
uniform vec3 lightPos;
uniform vec3 viewPos;
uniform vec3 lightColor;

void main() {
    // Componente ambiente
    float ambientStrength = 0.15;
    vec3 ambient = ambientStrength * lightColor;
    
    // Componente difuso (Lei de Lambert)
    vec3 norm = normalize(fragNormal);
    vec3 lightDir = normalize(lightPos - fragPosition);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;
    
    // Componente especular (Reflexão de Blinn-Phong)
    float specularStrength = 0.8;
    vec3 viewDir = normalize(viewPos - fragPosition);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);
    vec3 specular = specularStrength * spec * lightColor;
    
    // Iluminação final
    vec3 result = (ambient + diffuse + specular) * objectColor;
    fragColor = vec4(result, 1.0);
}
```

### Configuração de Estado OpenGL

```java
// Habilitação de depth testing (Z-buffer)
glEnable(GL_DEPTH_TEST);

// Cor de fundo (clear color)
glClearColor(0.1f, 0.1f, 0.12f, 1.0f);

// Loop de renderização
while (!glfwWindowShouldClose(handle)) {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    
    // Renderização da geometria
    shader.bind();
    setUniforms();
    mesh.render();
    shader.unbind();
    
    glfwSwapBuffers(handle);
    glfwPollEvents();
}
```

---

## Algoritmos Implementados

### 1. Geração Procedural de Icosaedro

O icosaedro regular é construído utilizando a **razão áurea** (golden ratio):

$$
\varphi = \frac{1 + \sqrt{5}}{2} \approx 1.618033988...
$$

**Propriedade Fundamental**: $\varphi^2 = \varphi + 1$

**12 Vértices** organizados em 3 retângulos ortogonais:

$$
\begin{aligned}
\text{Plano XY:} \quad &(\pm 1, \pm \varphi, 0) \\
\text{Plano YZ:} \quad &(0, \pm 1, \pm \varphi) \\
\text{Plano XZ:} \quad &(\pm \varphi, 0, \pm 1)
\end{aligned}
$$

Todos os vértices são normalizados para a esfera unitária:

$$
\vec{v}_{\text{normalized}} = \frac{\vec{v}}{|\vec{v}|} = \frac{\vec{v}}{\sqrt{x^2 + y^2 + z^2}}
$$

### 2. Subdivisão Recursiva de Triângulos

**Algoritmo de Subdivisão**:

Dado um triângulo $T(\vec{v}_1, \vec{v}_2, \vec{v}_3)$:

1. **Calcular pontos médios**:
   $$
   \begin{aligned}
   \vec{m}_{12} &= \text{normalize}\left(\frac{\vec{v}_1 + \vec{v}_2}{2}\right) \\
   \vec{m}_{23} &= \text{normalize}\left(\frac{\vec{v}_2 + \vec{v}_3}{2}\right) \\
   \vec{m}_{31} &= \text{normalize}\left(\frac{\vec{v}_3 + \vec{v}_1}{2}\right)
   \end{aligned}
   $$

2. **Gerar 4 novos triângulos**:
   $$
   \begin{aligned}
   T_1 &= (\vec{v}_1, \vec{m}_{12}, \vec{m}_{31}) \\
   T_2 &= (\vec{v}_2, \vec{m}_{23}, \vec{m}_{12}) \\
   T_3 &= (\vec{v}_3, \vec{m}_{31}, \vec{m}_{23}) \\
   T_4 &= (\vec{m}_{12}, \vec{m}_{23}, \vec{m}_{31})
   \end{aligned}
   $$

**Crescimento Exponencial**:

$$
\begin{aligned}
V(n) &= 10 \times 4^n + 2 && \text{(número de vértices)} \\
T(n) &= 20 \times 4^n && \text{(número de triângulos)}
\end{aligned}
$$

**Tabela de Complexidade**:

| Subdivisões | Vértices | Triângulos | Arestas |
|-------------|----------|------------|---------|
| 0 | 12 | 20 | 30 |
| 1 | 42 | 80 | 120 |
| 2 | 162 | 320 | 480 |
| 3 | 642 | 1.280 | 1.920 |
| 4 | 2.562 | 5.120 | 7.680 |
| 5 | 10.242 | 20.480 | 30.720 |

### 3. Cache de Pontos Médios (Otimização Crítica)

**Problema**: Triângulos adjacentes compartilham arestas, resultando em cálculo redundante de pontos médios.

**Solução**: HashMap com chaves baseadas em índices de vértices.

**Geração de Chave Única**:

```java
// Bit shifting para criar chave de 64 bits
long key = ((long)Math.min(p1, p2) << 32) | Math.max(p1, p2);
```

**Representação Matemática**:

$$
\text{key}(v_i, v_j) = \begin{cases}
(i \ll 32) + j & \text{se } i < j \\
(j \ll 32) + i & \text{se } j < i
\end{cases}
$$

**Análise de Complexidade**:

- **Sem cache**: $O(E \cdot n)$ onde $E$ = número de arestas, $n$ = número de subdivisões
- **Com cache**: $O(E)$ — cada aresta processada exatamente uma vez
- **Lookup**: $O(1)$ amortizado (HashMap)

**Implementação**:

```java
private static int getMiddlePoint(int p1, int p2, List<Vector3f> vertices, 
                                   Map<Long, Integer> cache) {
    // Ordenação garante chave consistente
    boolean firstIsSmaller = p1 < p2;
    long smallerIndex = firstIsSmaller ? p1 : p2;
    long greaterIndex = firstIsSmaller ? p2 : p1;
    long key = (smallerIndex << 32) + greaterIndex;
    
    // Consulta no cache
    if (cache.containsKey(key)) {
        return cache.get(key);
    }
    
    // Cálculo do ponto médio normalizado
    Vector3f v1 = vertices.get(p1);
    Vector3f v2 = vertices.get(p2);
    Vector3f middle = new Vector3f(
        (v1.x + v2.x) / 2.0f,
        (v1.y + v2.y) / 2.0f,
        (v1.z + v2.z) / 2.0f
    ).normalize();
    
    // Inserção no cache
    int index = vertices.size();
    vertices.add(middle);
    cache.put(key, index);
    
    return index;
}
```

**Ganho de Performance**:
- Subdivisão 3: ~50% redução em operações de vértice
- Subdivisão 4: ~60% redução em operações de vértice
- Redução de memória: Sem duplicatas de vértices

---

## Modelo de Iluminação

### Equação de Phong

$$
\mathbf{I} = \mathbf{I}_a + \mathbf{I}_d + \mathbf{I}_s
$$

Onde:
- $\mathbf{I}_a$ — Componente ambiente (ambient)
- $\mathbf{I}_d$ — Componente difuso (diffuse)
- $\mathbf{I}_s$ — Componente especular (specular)

### Componente Ambiente

Simulação simplificada de iluminação indireta:

$$
\mathbf{I}_a = k_a \cdot \mathbf{L}_{\text{color}}
$$

- $k_a = 0.15$ — Coeficiente ambiente
- $\mathbf{L}_{\text{color}}$ — Cor da fonte de luz

### Componente Difuso (Lei de Lambert)

Reflexão lambertiana — superfícies perfeitamente difusas:

$$
\mathbf{I}_d = k_d \cdot (\mathbf{N} \cdot \mathbf{L}) \cdot \mathbf{L}_{\text{color}}
$$

Onde:
- $\mathbf{N}$ — Normal da superfície (normalizada)
- $\mathbf{L}$ — Direção da luz (normalizada): $\mathbf{L} = \text{normalize}(\mathbf{P}_{\text{light}} - \mathbf{P}_{\text{fragment}})$
- $k_d = \max(\mathbf{N} \cdot \mathbf{L}, 0)$ — Termo cosseno (clamped)

**Interpretação Física**: Intensidade proporcional ao ângulo entre a normal e a direção da luz.

### Componente Especular (Modelo de Phong)

Reflexão especular para simular brilho:

$$
\mathbf{I}_s = k_s \cdot (\mathbf{R} \cdot \mathbf{V})^\alpha \cdot \mathbf{L}_{\text{color}}
$$

Onde:
- $\mathbf{R}$ — Vetor de reflexão: $\mathbf{R} = 2(\mathbf{N} \cdot \mathbf{L})\mathbf{N} - \mathbf{L}$
- $\mathbf{V}$ — Direção da visão (câmera): $\mathbf{V} = \text{normalize}(\mathbf{P}_{\text{camera}} - \mathbf{P}_{\text{fragment}})$
- $\alpha = 32$ — Expoente de especularidade (shininess)
- $k_s = 0.8$ — Intensidade especular

**Comportamento do Expoente**:
- $\alpha$ pequeno (1-10): Superfície fosca, reflexão dispersa
- $\alpha$ médio (20-50): Metal escovado
- $\alpha$ grande (100+): Espelho, reflexão concentrada

### Cálculo do Vetor de Reflexão

Derivação geométrica:

$$
\begin{aligned}
\mathbf{R} &= \mathbf{L} - 2 \cdot \text{proj}_{\mathbf{N}}(\mathbf{L}) \\
&= \mathbf{L} - 2(\mathbf{L} \cdot \mathbf{N})\mathbf{N}
\end{aligned}
$$

No GLSL, a função `reflect()` implementa:

```glsl
vec3 reflectDir = reflect(-lightDir, norm);
```

**Nota**: O sinal negativo em `-lightDir` é necessário porque `reflect()` espera o vetor **incidente** (apontando para a superfície).

### Iluminação Final

$$
\mathbf{I}_{\text{final}} = (\mathbf{I}_a + \mathbf{I}_d + \mathbf{I}_s) \odot \mathbf{C}_{\text{object}}
$$

Onde $\odot$ denota multiplicação componente-a-componente (Hadamard product).

---

## Sistema de Câmera

### Coordenadas Esféricas

Representação da posição da câmera em torno do objeto:

$$
\begin{aligned}
x &= r \cdot \sin(\theta) \cdot \cos(\phi) \\
y &= r \cdot \cos(\theta) \\
z &= r \cdot \sin(\theta) \cdot \sin(\phi)
\end{aligned}
$$

**Parâmetros**:
- $r \in [r_{\min}, r_{\max}]$ — Distância radial (zoom)
- $\theta \in [\epsilon, \pi - \epsilon]$ — Ângulo polar (vertical), $\epsilon = 0.1$ rad para evitar gimbal lock
- $\phi \in [0, 2\pi)$ — Ângulo azimutal (horizontal)

**Domínios Implementados**:
- $r_{\min} = 1.5$, $r_{\max} = 10.0$
- $\theta_{\min} = 0.1$, $\theta_{\max} = 3.04$ rad

### Mapeamento de Input do Mouse

**Rotação**:

$$
\begin{aligned}
\phi_{\text{new}} &= \phi_{\text{old}} + \Delta x \cdot s \\
\theta_{\text{new}} &= \text{clamp}(\theta_{\text{old}} + \Delta y \cdot s, \theta_{\min}, \theta_{\max})
\end{aligned}
$$

Onde $s = 0.005$ é o fator de sensibilidade.

**Zoom** (scroll wheel):

$$
r_{\text{new}} = \text{clamp}(r_{\text{old}} - \Delta_{\text{scroll}} \cdot z, r_{\min}, r_{\max})
$$

Onde $z = 0.5$ é o fator de zoom.

### Construção da Matriz View

```java
public Matrix4f getViewMatrix() {
    Vector3f position = getPosition();
    Vector3f target = new Vector3f(0, 0, 0);
    Vector3f up = new Vector3f(0, 1, 0);
    
    return new Matrix4f().lookAt(position, target, up);
}
```

**Benefícios da Parametrização Esférica**:
1. Garante que a câmera sempre olha para a origem
2. Evita singularidades (gimbal lock) com clamping de $\theta$
3. Controle intuitivo: $\phi$ para girar horizontalmente, $\theta$ para girar verticalmente
4. Implementação comum em software 3D (Blender, Maya, Unity)

---

## Otimizações de Performance

### 1. Vertex Buffer Objects (VBO) — Memória GPU

**Estratégia**: Dados de geometria armazenados permanentemente na VRAM.

```java
vboId = glGenBuffers();
glBindBuffer(GL_ARRAY_BUFFER, vboId);
glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
```

**GL_STATIC_DRAW**: Hint para o driver que os dados não serão modificados, permitindo otimizações internas.

**Ganho**: Eliminação de transferências CPU→GPU a cada frame.

### 2. Element Buffer Objects (EBO) — Indexed Drawing

**Problema**: Vértices compartilhados entre triângulos resultam em duplicação de dados.

**Solução**: Indexação de vértices.

**Exemplo (cubo)**:
- Sem EBO: 36 vértices (6 faces × 2 triângulos × 3 vértices)
- Com EBO: 24 vértices + 36 índices

**Economia**:
$$
\text{Economia} = \frac{36 \times \text{sizeof(Vertex)} - (24 \times \text{sizeof(Vertex)} + 36 \times \text{sizeof(int)})}{36 \times \text{sizeof(Vertex)}} \times 100\%
$$

Para vértices com 6 floats (posição + normal):
$$
\text{Economia} = \frac{36 \times 24 - (24 \times 24 + 36 \times 4)}{36 \times 24} \times 100\% \approx 50\%
$$

### 3. Vertex Array Objects (VAO) — Estado de Renderização

**Conceito**: Encapsula todo o estado de configuração de vértices em um único objeto.

```java
vaoId = glGenVertexArrays();
glBindVertexArray(vaoId);

// Configuração de atributos
glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);  // Position
glEnableVertexAttribArray(0);
glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);  // Normal
glEnableVertexAttribArray(1);

glBindVertexArray(0);  // Unbind
```

**Vantagem**: Uma única chamada `glBindVertexArray(vaoId)` restaura toda a configuração.

**Performance**: Reduz overhead de chamadas OpenGL por frame.

### 4. Pré-alocação de Memória

```java
int estimatedVertices = 12 * (int)Math.pow(4, subdivisions);
List<Vector3f> vertices = new ArrayList<>(estimatedVertices);
```

**Benefício**: Evita realocações dinâmicas da ArrayList durante crescimento.

**Complexidade**:
- Sem pré-alocação: $O(n \log n)$ — realocações sucessivas
- Com pré-alocação: $O(n)$ — inserção direta

### 5. Cache de Pontos Médios (Já Descrito)

Redução de $O(n^2)$ para $O(n)$ na geração de icosfera.

### Métricas de Performance

| Métrica | Valor | Observação |
|---------|-------|------------|
| Frame Rate | 60 FPS | V-Sync habilitado |
| Frame Time | ~16.67 ms | 1/60 segundos |
| Draw Calls | 2 | Sólido + wireframe |
| Triângulos (subdiv. 3) | 1.280 | Icosfera padrão |
| Memória GPU | ~50 KB | Por mesh |
| Vértices processados/s | 76.800 | 1.280 × 60 |

## Instruções de Execução

### Pré-requisitos

- **Java Development Kit (JDK)**: Versão 17 ou superior
- **Apache Maven**: Versão 3.6 ou superior
- **Placa Gráfica**: Suporte a OpenGL 3.3 Core Profile
- **Sistema Operacional**: Windows, Linux ou macOS

### Verificação de Versões

```bash
# Java
java -version
# Saída esperada: java version "17.x.x" ou superior

# Maven
mvn -version
# Saída esperada: Apache Maven 3.x.x
```

### Compilação e Execução

#### Método 1: Maven Exec Plugin

```bash
# Clone o repositório
git clone https://github.com/seu-usuario/GeometryForge.git
cd GeometryForge

# Compile e execute
mvn clean compile exec:java
```

#### Método 2: JAR Executável

```bash
# Gerar JAR com dependências
mvn clean package

# Executar
java -jar target/GeometryForge-1.0.jar
```

#### Método 3: IDE (IntelliJ IDEA / Eclipse)

1. Importe o projeto como projeto Maven existente
2. Aguarde a resolução automática de dependências
3. Execute a classe `Main.java`

### Controles da Aplicação

| Entrada | Ação | Descrição Técnica |
|---------|------|-------------------|
| **Click esquerdo + arrastar** | Rotação orbital | Modifica $\theta$ e $\phi$ da câmera |
| **Scroll do mouse** | Zoom | Ajusta $r$ (distância radial) |
| **ESPAÇO** | Alternar geometria | Comuta entre cubo e icosfera |
| **ESC** | Sair | Encerra a aplicação |

### Configurações Avançadas

```java
// Main.java - Constantes de configuração

// Subdivisões do icosaedro (0-5 recomendado)
private static final int ICOSPHERE_SUBDIVISIONS = 3;

// Câmera
private static final float CAMERA_ROTATION_SENSITIVITY = 0.005f;
private static final float CAMERA_ZOOM_SENSITIVITY = 0.5f;

// Iluminação
private static final Vector3f FIXED_LIGHT_POSITION = new Vector3f(5.0f, 5.0f, 5.0f);
```

### Solução de Problemas

**Erro: "Unable to initialize GLFW"**
- Causa: Drivers gráficos desatualizados
- Solução: Atualizar drivers da placa de vídeo

**Erro: "Shader compile error"**
- Causa: Incompatibilidade de versão GLSL
- Solução: Verificar suporte a OpenGL 3.3

**Performance baixa (<30 FPS)**
- Causa: Subdivisões excessivas do icosaedro
- Solução: Reduzir `ICOSPHERE_SUBDIVISIONS` para 2 ou 1

---

## Especificações Técnicas

### Stack Tecnológico

| Componente | Tecnologia | Versão | Propósito |
|------------|-----------|--------|-----------|
| Linguagem | Java | 17+ | Linguagem principal |
| Build Tool | Maven | 3.6+ | Gerenciamento de dependências |
| Graphics API | OpenGL | 3.3 Core | API de renderização |
| Windowing | GLFW | 3.3.3 | Gerenciamento de janelas e input |
| Math Library | JOML | 1.10.5 | Operações vetoriais/matriciais |
| Bindings | LWJGL | 3.3.3 | Java bindings para APIs nativas |
| Logging | SLF4J | 2.0+ | Sistema de logs |

### Dependências Maven

```xml
    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <lwjgl.version>3.3.6</lwjgl.version>
        <joml.version>1.10.5</joml.version>
    </properties>

    <!-- OS-specific profiles for native libs -->
    <profiles>
        <profile>
            <id>windows</id>
            <activation>
                <os>
                    <family>windows</family>
                </os>
            </activation>
            <properties>
                <native.target>natives-windows</native.target>
            </properties>
        </profile>
        <profile>
            <id>linux</id>
            <activation>
                <os>
                    <family>unix</family>
                </os>
            </activation>
            <properties>
                <native.target>natives-linux</native.target>
            </properties>
        </profile>
        <profile>
            <id>macos</id>
            <activation>
                <os>
                    <family>mac</family>
                </os>
            </activation>
            <properties>
                <native.target>natives-macos</native.target>
            </properties>
        </profile>
    </profiles>

    <dependencies>
        <!-- LWJGL core -->
        <dependency>
            <groupId>org.lwjgl</groupId>
            <artifactId>lwjgl</artifactId>
            <version>${lwjgl.version}</version>
        </dependency>
        <dependency>
            <groupId>org.lwjgl</groupId>
            <artifactId>lwjgl</artifactId>
            <version>${lwjgl.version}</version>
            <classifier>${native.target}</classifier>
        </dependency>
        <!-- GLFW (window + input) -->
        <dependency>
            <groupId>org.lwjgl</groupId>
            <artifactId>lwjgl-glfw</artifactId>
            <version>${lwjgl.version}</version>
        </dependency>
        <dependency>
            <groupId>org.lwjgl</groupId>
            <artifactId>lwjgl-glfw</artifactId>
            <version>${lwjgl.version}</version>
            <classifier>${native.target}</classifier>
        </dependency>
        <!-- OpenGL -->
        <dependency>
            <groupId>org.lwjgl</groupId>
            <artifactId>lwjgl-opengl</artifactId>
            <version>${lwjgl.version}</version>
        </dependency>
        <dependency>
            <groupId>org.lwjgl</groupId>
            <artifactId>lwjgl-opengl</artifactId>
            <version>${lwjgl.version}</version>
            <classifier>${native.target}</classifier>
        </dependency>
        <!-- JOML: math library (vectors, matrices) -->
        <dependency>
            <groupId>org.joml</groupId>
            <artifactId>joml</artifactId>
            <version>${joml.version}</version>
        </dependency>
    </dependencies>
```

### Estrutura de Diretórios

```
GeometryForge/
│
├── src/
│   └── main/
│       ├── java/
│       │   ├── Main.java
│       │   ├── core/
│       │   │   ├── Window.java
│       │   │   ├── Camera.java
│       │   │   └── ShaderProgram.java
│       │   └── geometry/
│       │       ├── Shape.java
│       │       ├── Mesh.java
│       │       ├── Cube.java
│       │       └── Icosphere.java
│       │
│       └── resources/
│           └── shaders/
│               ├── vertex.glsl
│               └── fragment.glsl
│
├── pom.xml
├── README.md
└── .gitignore
```

### Requisitos de Sistema

**Mínimos**:
- CPU: Dual-core 2.0 GHz
- RAM: 2 GB
- GPU: OpenGL 3.3 compatível
- Espaço em disco: 100 MB

**Recomendados**:
- CPU: Quad-core 2.5 GHz+
- RAM: 4 GB
- GPU: Dedicada com suporte a OpenGL 4.0+
- Espaço em disco: 200 MB

---

## Conceitos Avançados Aplicados

### 1. Graphics Pipeline Programmable

**Estágios Implementados**:
- **Vertex Shader**: Transformações geométricas (MVP), cálculo de normais
- **Fragment Shader**: Iluminação per-pixel (Phong), cálculo de cores

**Estágios Fixos** (gerenciados pelo OpenGL):
- **Primitive Assembly**: Montagem de triângulos
- **Rasterization**: Conversão de primitivas em fragmentos
- **Depth Testing**: Z-buffer para oclusão
- **Blending**: Composição de cores (não utilizado neste projeto)

### 2. Espaços de Coordenadas

**Pipeline Completo**:

$$
\text{Object Space} \xrightarrow{M_{\text{model}}} \text{World Space} \xrightarrow{M_{\text{view}}} \text{Camera Space} \xrightarrow{M_{\text{proj}}} \text{Clip Space} \xrightarrow{\div w} \text{NDC} \xrightarrow{\text{viewport}} \text{Screen Space}
$$

### 3. Buffers de Renderização

- **Color Buffer**: RGB(A) de cada pixel
- **Depth Buffer**: Profundidade Z para ordenação correta
- **Stencil Buffer**: Não utilizado (reservado para efeitos avançados)

### 4. Interpolação de Atributos

**Perspectiva-Correta**: Atributos (normais, posições) são interpolados com correção de perspectiva automaticamente pelo rasterizador.

$$
\text{attr}_{\text{fragment}} = \frac{\sum_i \frac{w_i \cdot \text{attr}_i}{z_i}}{\sum_i \frac{w_i}{z_i}}
$$

Onde $w_i$ são coordenadas baricêntricas e $z_i$ são profundidades dos vértices.

---

## Licença

Este projeto é distribuído sob a **MIT License**:

MIT License

Copyright (c) 2026 Miquéias Alves Medeiros

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.

---

## Autor

Miquéias Alves Medeiros
Backend Developer

- LinkedIn: www.linkedin.com/in/miquéiasal
- Email: contatomiqueiasalvesdev@gmail.com
