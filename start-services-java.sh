#!/bin/bash

# Directorios base
BASE_DIR="/Users/mgmn/GFT/Workspaces/saga-architecture"
JAVA_DIR="$BASE_DIR/saga-architecture-java"
INFRA_DIR="$BASE_DIR/saga-architecture-infra"
ANGULAR_DIR="$BASE_DIR/saga-architecture-angular"

echo "🚀 Iniciando el Ecosistema Completo (Infra + Java + Angular)..."

# Función para abrir una nueva pestaña en Terminal de macOS y ejecutar un comando
run_in_tab() {
    local dir=$1
    local cmd=$2
    local title=$3
    
    osascript <<EOF
tell application "Terminal"
    activate
    -- Abrir una nueva pestaña
    tell application "System Events" to keystroke "t" using {command down}
    delay 0.5
    -- Ejecutar el comando en la pestaña activa
    do script "cd $dir && printf '\\\e]1;$title\\\a' && $cmd" in front window
end tell
EOF
}

# 0. Levantar Infraestructura (Kafka, Zookeeper, etc.)
echo "⏳ Levantando Docker Compose..."
run_in_tab "$INFRA_DIR" "docker-compose up" "INFRA-DOCKER"

# Esperamos a que Kafka esté listo
echo "⏱️ Esperando 15 segundos para que Kafka se estabilice..."
sleep 15

# Pre-crear topics para evitar fallos de provisionamiento en Spring
echo "📦 Pre-creando topics en Kafka..."
docker exec saga-kafka kafka-topics --bootstrap-server 127.0.0.1:9094 --create --if-not-exists --topic order-events --partitions 1 --replication-factor 1
docker exec saga-kafka kafka-topics --bootstrap-server 127.0.0.1:9094 --create --if-not-exists --topic payment-events --partitions 1 --replication-factor 1
docker exec saga-kafka kafka-topics --bootstrap-server 127.0.0.1:9094 --create --if-not-exists --topic inventory-events --partitions 1 --replication-factor 1
docker exec saga-kafka kafka-topics --bootstrap-server 127.0.0.1:9094 --create --if-not-exists --topic payment-commands --partitions 1 --replication-factor 1
docker exec saga-kafka kafka-topics --bootstrap-server 127.0.0.1:9094 --create --if-not-exists --topic inventory-commands --partitions 1 --replication-factor 1

# 1. Order Service (Puerto 8080)
run_in_tab "$JAVA_DIR/order-service" "mvn spring-boot:run" "ORDER-SVC"

# 2. Payment Service (Puerto 8081)
run_in_tab "$JAVA_DIR/payment-service" "mvn spring-boot:run" "PAYMENT-SVC"

# 3. Inventory Service (Puerto 8082)
run_in_tab "$JAVA_DIR/inventory-service" "mvn spring-boot:run" "INVENTORY-SVC"

# 4. Angular Frontend (Puerto 4200)
# Usando pnpm start ya que es el que instalamos
echo "🎨 Lanzando Frontend Angular..."
run_in_tab "$ANGULAR_DIR" "pnpm start" "ANGULAR-UI"

echo "✅ 'Full Combo' iniciado. Revisa las 5 pestañas de tu Terminal."
