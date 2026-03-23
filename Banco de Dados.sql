-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: sistema_estoque
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `categoria`
--

DROP TABLE IF EXISTS `categoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categoria` (
  `id_categoria` int NOT NULL AUTO_INCREMENT,
  `nome_categoria` varchar(50) NOT NULL,
  PRIMARY KEY (`id_categoria`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categoria`
--

LOCK TABLES `categoria` WRITE;
/*!40000 ALTER TABLE `categoria` DISABLE KEYS */;
INSERT INTO `categoria` VALUES (1,'Eletrônicos'),(2,'Informática'),(3,'Casa e Jardim'),(4,'Esporte e Lazer'),(5,'Livros e Revistas');
/*!40000 ALTER TABLE `categoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fornecedores`
--

DROP TABLE IF EXISTS `fornecedores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fornecedores` (
  `id_fornecedor` int NOT NULL AUTO_INCREMENT,
  `nome_fornecedor` varchar(100) NOT NULL,
  `endereco` text,
  `telefone` varchar(30) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `cnpj` char(18) DEFAULT NULL,
  `status` enum('Ativo','Inativo') DEFAULT 'Ativo',
  PRIMARY KEY (`id_fornecedor`),
  UNIQUE KEY `cnpj` (`cnpj`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fornecedores`
--

LOCK TABLES `fornecedores` WRITE;
/*!40000 ALTER TABLE `fornecedores` DISABLE KEYS */;
INSERT INTO `fornecedores` VALUES (1,'Tech Solutions Ltda','Rua das Flores, 123 - São Paulo/SP','(11) 98765-4321','contato@techsolutions.com','12.345.678/0001-90','Ativo'),(2,'Distribuidora Alpha','Av. Brasil, 456 - Rio de Janeiro/RJ','(21) 91234-5678','vendas@alpha.com.br','98.765.432/0001-10','Ativo'),(3,'Mega Suprimentos','Rua do Comércio, 789 - Belo Horizonte/MG','(31) 99988-7766','compras@megasuprimentos.com','11.222.333/0001-44','Ativo'),(4,'Fornecedor Beta','Rua da Paz, 321 - Salvador/BA','(71) 95555-3333','beta@fornecedor.com','44.555.666/0001-77','Inativo'),(5,'Global Tech','Av. Paulista, 1000 - São Paulo/SP','(11) 94444-2222','global@tech.com.br','77.888.999/0001-11','Ativo');
/*!40000 ALTER TABLE `fornecedores` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movimentacao_estoque`
--

DROP TABLE IF EXISTS `movimentacao_estoque`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimentacao_estoque` (
  `id_movimentacao` int NOT NULL AUTO_INCREMENT,
  `id_produto` int DEFAULT NULL,
  `data_movimentacao` datetime DEFAULT CURRENT_TIMESTAMP,
  `tipo_movimentacao` enum('Entrada','Saida') NOT NULL,
  `quantidade` int NOT NULL,
  `observacao` text,
  `id_usuario` int DEFAULT NULL,
  PRIMARY KEY (`id_movimentacao`),
  KEY `id_produto` (`id_produto`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `movimentacao_estoque_ibfk_1` FOREIGN KEY (`id_produto`) REFERENCES `produtos` (`id_produto`),
  CONSTRAINT `movimentacao_estoque_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimentacao_estoque`
--

LOCK TABLES `movimentacao_estoque` WRITE;
/*!40000 ALTER TABLE `movimentacao_estoque` DISABLE KEYS */;
INSERT INTO `movimentacao_estoque` VALUES (1,1,'2026-02-01 12:33:35','Entrada',15,'Entrada inicial - NF001-2024',1),(2,2,'2026-02-01 12:33:35','Entrada',8,'Entrada inicial - NF002-2024',2),(3,3,'2026-02-01 12:33:35','Entrada',5,'Entrada inicial - NF003-2024',1),(4,1,'2026-02-01 12:33:35','Saida',2,'Venda para cliente João',2),(5,4,'2026-02-01 12:33:35','Entrada',20,'Entrada inicial - NF004-2024',1),(6,1,'2026-03-21 19:08:31','Entrada',151,'Ex: NF-03821, reposição...',1),(7,1,'2026-03-21 19:09:10','Entrada',111,'Ex: NF-03821, reposição...',1),(8,1,'2026-03-21 19:09:58','Saida',166,'vendas',1),(9,1,'2026-03-21 19:10:55','Entrada',166,'lote',1),(10,2,'2026-03-23 17:22:41','Entrada',1,'Ex: NF-03821, reposição...',1);
/*!40000 ALTER TABLE `movimentacao_estoque` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nota_fiscal`
--

DROP TABLE IF EXISTS `nota_fiscal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nota_fiscal` (
  `id_nf` int NOT NULL AUTO_INCREMENT,
  `numero_nf` varchar(50) NOT NULL,
  `data_emissao` date NOT NULL,
  `valor_total` decimal(10,2) DEFAULT NULL,
  `id_fornecedor` int DEFAULT NULL,
  PRIMARY KEY (`id_nf`),
  UNIQUE KEY `numero_nf` (`numero_nf`),
  KEY `id_fornecedor` (`id_fornecedor`),
  CONSTRAINT `nota_fiscal_ibfk_1` FOREIGN KEY (`id_fornecedor`) REFERENCES `fornecedores` (`id_fornecedor`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nota_fiscal`
--

LOCK TABLES `nota_fiscal` WRITE;
/*!40000 ALTER TABLE `nota_fiscal` DISABLE KEYS */;
INSERT INTO `nota_fiscal` VALUES (1,'NF001-2024','2024-01-15',18000.00,1),(2,'NF002-2024','2024-01-20',25600.00,2),(3,'NF003-2024','2024-02-05',6000.00,3),(4,'NF004-2024','2024-02-10',9000.00,1),(5,'NF005-2024','2024-02-15',1440.00,5);
/*!40000 ALTER TABLE `nota_fiscal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `produtos`
--

DROP TABLE IF EXISTS `produtos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `produtos` (
  `id_produto` int NOT NULL AUTO_INCREMENT,
  `nome_produto` varchar(100) NOT NULL,
  `descricao` text,
  `unidade_medida` varchar(10) DEFAULT NULL,
  `id_categoria` int DEFAULT NULL,
  `marca` varchar(50) DEFAULT NULL,
  `preco_custo` decimal(10,2) DEFAULT NULL,
  `preco_venda` decimal(10,2) DEFAULT NULL,
  `quantidade` int DEFAULT '0',
  `status` enum('Ativo','Inativo') DEFAULT 'Ativo',
  PRIMARY KEY (`id_produto`),
  KEY `id_categoria` (`id_categoria`),
  CONSTRAINT `produtos_ibfk_1` FOREIGN KEY (`id_categoria`) REFERENCES `categoria` (`id_categoria`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `produtos`
--

LOCK TABLES `produtos` WRITE;
/*!40000 ALTER TABLE `produtos` DISABLE KEYS */;
INSERT INTO `produtos` VALUES (1,'Smartphone Galaxy S23','Smartphone Samsung Galaxy S23 128GB','UN',1,'Samsung',1200.00,1800.00,277,'Ativo'),(2,'Notebook Dell Inspiron','Notebook Dell Inspiron 15 i5 8GB 256GB SSD','UN',2,'Dell',2500.00,3200.00,9,'Ativo'),(3,'Cadeira Gamer','Cadeira Gamer RGB com LED','UN',3,'DXRacer',800.00,1200.00,5,'Ativo'),(4,'Tênis Nike Air Max','Tênis Nike Air Max 270 Masculino','PAR',4,'Nike',300.00,450.00,20,'Ativo'),(5,'Livro Java Como Programar','Livro Deitel Java Como Programar 10ª Ed','UN',5,'Pearson',80.00,120.00,12,'Ativo'),(6,'MOUSE ADVANCED','Switch Blue RGB','UN',1,'Logitech',150.00,299.90,50,'Ativo'),(9,'Teclado Mecânico','Switch Blue RGB','Un',1,'Logitech',150.00,299.90,50,'Ativo');
/*!40000 ALTER TABLE `produtos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id_usuario` int NOT NULL AUTO_INCREMENT,
  `nome_usuario` varchar(50) NOT NULL,
  `login` varchar(30) NOT NULL,
  `senha` varchar(255) NOT NULL,
  `nivel_acesso` enum('Administrador','Operador','Consulta') DEFAULT 'Consulta',
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `login` (`login`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'João Silva','joao.silva','senha123','Administrador'),(2,'Maria Santos','maria.santos','maria456','Operador'),(3,'Pedro Oliveira','pedro.oliveira','pedro789','Consulta'),(4,'Ana Costa','ana.costa','ana321','Operador'),(5,'Carlos Mendes','carlos.mendes','carlos654','Consulta');
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-23 17:51:52
