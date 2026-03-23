# 📦 Sistema de Controle de Estoque - Loja de TI

Projeto Integrador sendo desenvolvido para o meu curso do Senac.
O sistema tem como objetivo realizar o controle de estoque de uma loja de produtos de informática e tecnologia.

---
 Tecnologias Utilizadas:
 JAVA SE, JAVA SWING, MYSQL, JBDC, e Netbeans.

 👥 Time de Desenvolvedores
Nome: Cauã Souza da Silva , Função:Desenvolvedor Full Stack




⚙️ Funcionalidades do Sistema
🔐 Autenticação

Login com validação de usuário e senha no banco de dados
Controle de acesso por nível: Administrador, Operador e Consulta
Redirecionamento automático para o Dashboard após login

📊 Dashboard

Painel com KPIs em tempo real:

Total de produtos ativos
Valor total do estoque
Alertas de estoque crítico
Movimentações do dia


Lista das últimas movimentações
Alertas de produtos com estoque zerado ou abaixo do mínimo
Ações rápidas para registrar entrada, saída ou novo produto

📦 Produtos

Listagem completa com busca por nome e marca
Filtro por status (Ativo / Inativo)
Cadastro e edição com todos os campos:

Nome, descrição, marca, categoria, unidade de medida
Preço de custo e preço de venda (BigDecimal)
Quantidade em estoque e status


Cálculo automático da margem de lucro (calcularMargemLucro())

🔄 Movimentações de Estoque

Registro de Entrada e Saída de produtos
Validação de estoque suficiente antes de confirmar saída
Atualização automática da quantidade do produto após movimentação
Uso de transação SQL (commit/rollback) para garantir consistência
Histórico completo com filtro por tipo (Entrada / Saída)

🏭 Fornecedores

Listagem com busca por nome e CNPJ
Filtro por status (Ativo / Inativo)
Cadastro e edição: nome, CNPJ, endereço, telefone, email, status

🧾 Notas Fiscais

Listagem com KPIs: total de notas, notas do mês e valor total do mês
Busca por número da NF ou nome do fornecedor
Cadastro e edição: número, data de emissão (LocalDate), valor total (BigDecimal), fornecedor

👤 Usuários (restrito a Administradores)

Listagem com indicadores de permissão (podeOperar(), isAdministrador())
Filtro por nível de acesso
Cadastro e edição: nome, login, senha, nível de acesso
Matriz de permissões por nível
Proteção contra edição do próprio usuário logado

Ele permite o gerenciamento de:

- 📦 Produtos
- 🚚 Fornecedores
- 📥 Entradas de estoque
- 🔄 Movimentações
