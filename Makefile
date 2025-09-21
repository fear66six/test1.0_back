# ByrSki Project Makefile - 应用层部署工具
# 服务器配置
BACKEND_SERVER_IP ?= 182.92.210.229
SERVER_HOST = $(BACKEND_SERVER_IP)
SERVER_USER = root
PROJECT_NAME = byrski

# 版本管理 - 基于Git的语义化版本控制
GIT_TAG = $(shell git describe --tags --abbrev=0 2>/dev/null || echo "v1.0.0")
GIT_COMMIT = $(shell git rev-parse --short HEAD 2>/dev/null || echo "unknown")
GIT_BRANCH = $(shell git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "main")

# 生成一次性时间戳，确保整个构建过程中时间戳一致
BUILD_TIMESTAMP_FILE = .build_timestamp
BUILD_TIMESTAMP = $(shell if [ ! -f $(BUILD_TIMESTAMP_FILE) ]; then date +%Y%m%d_%H%M%S > $(BUILD_TIMESTAMP_FILE); fi; cat $(BUILD_TIMESTAMP_FILE))
BUILD_DATE = $(shell echo $(BUILD_TIMESTAMP) | cut -d'_' -f1)
BUILD_TIME = $(shell echo $(BUILD_TIMESTAMP) | cut -d'_' -f2)

# 清理版本标签（移除v前缀）
CLEAN_VERSION = $(shell echo $(GIT_TAG) | sed 's/^v//')

# 根据分支生成专业版本号
ifeq ($(GIT_BRANCH),main)
    # 主分支：使用发布版本 + 提交哈希
    APP_VERSION = $(CLEAN_VERSION)-$(GIT_COMMIT)
    DOCKER_TAG = $(CLEAN_VERSION)-$(BUILD_DATE).$(BUILD_TIME)-$(GIT_COMMIT)
else ifeq ($(GIT_BRANCH),develop)
    # 开发分支：使用RC版本 + 提交哈希
    APP_VERSION = $(CLEAN_VERSION)-RC.$(BUILD_DATE)-$(GIT_COMMIT)
    DOCKER_TAG = $(CLEAN_VERSION)-RC.$(BUILD_DATE).$(BUILD_TIME)-$(GIT_COMMIT)
else
    # 特性分支：使用SNAPSHOT版本 + 提交哈希
    APP_VERSION = $(CLEAN_VERSION)-SNAPSHOT-$(GIT_COMMIT)
    DOCKER_TAG = $(CLEAN_VERSION)-$(GIT_BRANCH)-$(GIT_COMMIT)
endif

TIMESTAMP = $(BUILD_TIMESTAMP)
VERSION_TAG = $(PROJECT_NAME):$(DOCKER_TAG)

# 平台配置
TARGET_PLATFORM = linux/amd64
CURRENT_PLATFORM = $(shell uname -m)

# 颜色定义
GREEN = \033[0;32m
YELLOW = \033[0;33m
RED = \033[0;31m
NC = \033[0m # No Color

# 默认目标
.PHONY: help deploy build push clean check-platform init-server version tag-release diagnose fix-server rebuild deploy-existing deploy-rebuild versions rollback logs status

help:
	@echo "$(GREEN)ByrSki 应用层部署工具$(NC)"
	@echo "可用命令:"
	@echo "  make version        - 显示当前版本信息"
	@echo "  make tag-release    - 创建新的版本标签"
	@echo "  make init-server    - 初始化服务器环境(首次部署时使用)"
	@echo ""
	@echo "$(GREEN)部署命令:$(NC)"
	@echo "  make deploy         - 部署应用 (可选择重新构建或使用现有镜像)"
	@echo "  make deploy-rebuild - 重新构建并部署应用"
	@echo "  make deploy-existing- 使用现有镜像部署应用"
	@echo ""
	@echo "$(YELLOW)其他命令:$(NC)"
	@echo "  make build          - 本地构建项目"
	@echo "  make rebuild        - 强制重新构建并部署(解决镜像问题)"
	@echo "  make versions       - 显示最近5个版本"
	@echo "  make rollback       - 回退到指定版本"
	@echo "  make clean          - 清理本地镜像"
	@echo "  make check-platform - 检查Docker多平台支持"
	@echo "  make diagnose       - 诊断服务器环境问题"
	@echo "  make fix-server     - 自动修复服务器Docker问题"
	@echo "  make logs           - 查看服务日志"
	@echo "  make status         - 查看服务状态"
	@echo ""
	@echo "$(GREEN)版本管理:$(NC)"
	@echo "  Git标签: $(GIT_TAG) → 应用版本: $(APP_VERSION)"
	@echo "  Git分支: $(GIT_BRANCH) ($(GIT_COMMIT))"
	@echo "  Docker标签: $(DOCKER_TAG)"
	@echo ""
	@echo "$(YELLOW)平台信息:$(NC)"
	@echo "  当前平台: $(CURRENT_PLATFORM)"
	@echo "  目标平台: $(TARGET_PLATFORM)"
	@if [ "$(CURRENT_PLATFORM)" != "x86_64" ]; then \
		echo "  $(YELLOW)⚠️  将进行交叉编译到Linux平台$(NC)"; \
	fi
	@echo ""
	@echo "$(YELLOW)版本命名规则:$(NC)"
	@echo "  main分支: $(CLEAN_VERSION) (发布版本)"
	@echo "  develop分支: $(CLEAN_VERSION)-RC.$(BUILD_DATE) (候选版本)"
	@echo "  其他分支: $(CLEAN_VERSION)-SNAPSHOT (快照版本)"
	@echo ""
	@echo "$(YELLOW)注意事项:$(NC)"
	@echo "  • 此工具仅部署应用层（后端服务+缓存）"
	@echo "  • 数据库层需要在单独的服务器上部署"
	@echo "  • 确保在.env文件中配置正确的DATABASE_SERVER_IP"

# 显示版本信息
version:
	@echo "$(GREEN)=== ByrSki 应用层版本信息 ===$(NC)"
	@echo ""
	@echo "$(YELLOW)Git 信息:$(NC)"
	@echo "  最新标签: $(GIT_TAG)"
	@echo "  当前分支: $(GIT_BRANCH)"
	@echo "  提交哈希: $(GIT_COMMIT)"
	@echo "  仓库状态: $$(if git diff-index --quiet HEAD --; then echo '$(GREEN)✅ 干净$(NC)'; else echo '$(YELLOW)⚠️  有未提交更改$(NC)'; fi)"
	@echo ""
	@echo "$(YELLOW)构建信息:$(NC)"
	@echo "  应用版本: $(APP_VERSION)"
	@echo "  Docker标签: $(DOCKER_TAG)"
	@echo "  构建日期: $(BUILD_DATE)"
	@echo "  构建时间: $(BUILD_TIME)"
	@echo ""
	@echo "$(YELLOW)部署信息:$(NC)"
	@echo "  应用服务器: $(SERVER_HOST)"
	@echo "  项目名: $(PROJECT_NAME)"
	@echo "  平台: $(CURRENT_PLATFORM) → $(TARGET_PLATFORM)"
	@echo ""
	@if [ -f "target/ByrSki-$(APP_VERSION).jar" ]; then \
		echo "$(GREEN)✅ JAR文件已构建: target/ByrSki-$(APP_VERSION).jar$(NC)"; \
		ls -lh target/ByrSki-$(APP_VERSION).jar | awk '{print "  文件大小: " $$5}'; \
	else \
		echo "$(YELLOW)⚠️  JAR文件未找到，运行 'make build' 进行构建$(NC)"; \
	fi

# 创建版本标签
tag-release:
	@echo "$(GREEN)创建新版本标签$(NC)"
	@echo "当前版本: $(GIT_TAG)"
	@echo ""
	@read -p "请输入新版本号 (格式: v1.2.3): " new_version; \
	if [ -z "$$new_version" ]; then \
		echo "$(RED)错误: 版本号不能为空$(NC)"; \
		exit 1; \
	fi; \
	if ! echo "$$new_version" | grep -qE '^v[0-9]+\.[0-9]+\.[0-9]+$$'; then \
		echo "$(RED)错误: 版本号格式不正确，应为 v1.2.3 格式$(NC)"; \
		exit 1; \
	fi; \
	echo "$(YELLOW)正在检查Git状态...$(NC)"; \
	if ! git diff-index --quiet HEAD --; then \
		echo "$(RED)错误: 工作目录有未提交的更改，请先提交所有更改$(NC)"; \
		exit 1; \
	fi; \
	if git tag -l | grep -q "$$new_version"; then \
		echo "$(RED)错误: 标签 $$new_version 已存在$(NC)"; \
		exit 1; \
	fi; \
	echo "$(YELLOW)创建标签: $$new_version$(NC)"; \
	git tag -a "$$new_version" -m "Release $$new_version"; \
	echo "$(GREEN)✅ 标签创建成功: $$new_version$(NC)"; \
	echo "$(YELLOW)提示: 运行 'git push origin $$new_version' 推送标签到远程仓库$(NC)"

# 构建项目
build:
	@echo "$(YELLOW)正在构建项目...$(NC)"
	@echo "$(GREEN)版本信息:$(NC)"
	@echo "  Git标签: $(GIT_TAG)"
	@echo "  Git分支: $(GIT_BRANCH)"
	@echo "  Git提交: $(GIT_COMMIT)"
	@echo "  应用版本: $(APP_VERSION)"
	@echo "  Docker标签: $(DOCKER_TAG)"
	@echo ""
	@echo "$(YELLOW)设置Java 17环境...$(NC)"
	@export JAVA_HOME=$$(if command -v /usr/libexec/java_home >/dev/null 2>&1; then /usr/libexec/java_home -v 17; else echo $$JAVA_HOME; fi) && \
	echo "使用Java版本: $$JAVA_HOME" && \
	echo "$(YELLOW)使用动态版本号构建: $(APP_VERSION)$(NC)" && \
	mvn clean package -DskipTests -Drevision=$(APP_VERSION)
	@echo "$(GREEN)项目构建完成 - 版本: $(APP_VERSION)$(NC)"

# 构建Docker镜像
build-image:
	@echo "$(YELLOW)正在构建Docker镜像...$(NC)"
	@echo "$(YELLOW)当前平台: $(CURRENT_PLATFORM), 目标平台: $(TARGET_PLATFORM)$(NC)"
	@if [ "$(CURRENT_PLATFORM)" != "x86_64" ] && [ "$(TARGET_PLATFORM)" = "linux/amd64" ]; then \
		echo "$(YELLOW)⚠️  正在进行交叉编译...$(NC)"; \
	fi
	docker build --platform $(TARGET_PLATFORM) -t $(VERSION_TAG) .
	docker tag $(VERSION_TAG) $(PROJECT_NAME):latest
	@echo "$(GREEN)Docker镜像构建完成: $(VERSION_TAG)$(NC)"

# 保存镜像并传输到服务器
push: build-image
	@echo "$(YELLOW)正在导出镜像...$(NC)"
	docker save $(VERSION_TAG) | gzip > $(PROJECT_NAME)_$(TIMESTAMP).tar.gz
	@echo "$(YELLOW)正在验证镜像文件完整性...$(NC)"
	@if ! gzip -t $(PROJECT_NAME)_$(TIMESTAMP).tar.gz; then \
		echo "$(RED)❌ 镜像文件压缩失败或损坏$(NC)"; \
		rm -f $(PROJECT_NAME)_$(TIMESTAMP).tar.gz; \
		exit 1; \
	fi
	@echo "$(GREEN)✅ 镜像文件完整性验证通过$(NC)"
	@echo "$(YELLOW)正在检查服务器连接...$(NC)"
	@if ! ssh $(SERVER_USER)@$(SERVER_HOST) "echo '服务器连接正常'" >/dev/null 2>&1; then \
		echo "$(RED)❌ 无法连接到服务器 $(SERVER_HOST)$(NC)"; \
		rm -f $(PROJECT_NAME)_$(TIMESTAMP).tar.gz; \
		exit 1; \
	fi
	@echo "$(YELLOW)正在检查服务器Docker服务状态...$(NC)"
	@if ! ssh $(SERVER_USER)@$(SERVER_HOST) "docker info >/dev/null 2>&1"; then \
		echo "$(RED)❌ 服务器Docker服务不可用$(NC)"; \
		rm -f $(PROJECT_NAME)_$(TIMESTAMP).tar.gz; \
		exit 1; \
	fi
	@echo "$(YELLOW)正在检查服务器磁盘空间...$(NC)"
	@ssh $(SERVER_USER)@$(SERVER_HOST) " \
		available_space=\$$(df /tmp | tail -n 1 | awk '{print \$$4}'); \
		file_size=\$$(ls -l $(PROJECT_NAME)_$(TIMESTAMP).tar.gz 2>/dev/null | awk '{print \$$5}' || echo 0); \
		file_size_kb=\$$((file_size / 1024)); \
		if [ \$$available_space -lt \$$((file_size_kb * 2)) ]; then \
			echo '❌ 服务器磁盘空间不足'; \
			echo \"可用空间: \$${available_space}KB, 需要空间: \$${file_size_kb}KB\"; \
			exit 1; \
		else \
			echo \"✅ 磁盘空间充足: \$${available_space}KB 可用\"; \
		fi"
	@echo "$(YELLOW)正在确保服务器目录存在...$(NC)"
	ssh $(SERVER_USER)@$(SERVER_HOST) "mkdir -p /root/$(PROJECT_NAME)"
	@echo "$(YELLOW)正在传输文件到服务器...$(NC)"
	scp $(PROJECT_NAME)_$(TIMESTAMP).tar.gz $(SERVER_USER)@$(SERVER_HOST):/tmp/
	scp docker-compose-app.yml $(SERVER_USER)@$(SERVER_HOST):/root/$(PROJECT_NAME)/docker-compose.yml
	@echo "$(YELLOW)正在验证传输文件完整性...$(NC)"
	@if ! ssh $(SERVER_USER)@$(SERVER_HOST) "gzip -t /tmp/$(PROJECT_NAME)_$(TIMESTAMP).tar.gz"; then \
		echo "$(RED)❌ 传输文件损坏，重新尝试传输...$(NC)"; \
		scp $(PROJECT_NAME)_$(TIMESTAMP).tar.gz $(SERVER_USER)@$(SERVER_HOST):/tmp/; \
		if ! ssh $(SERVER_USER)@$(SERVER_HOST) "gzip -t /tmp/$(PROJECT_NAME)_$(TIMESTAMP).tar.gz"; then \
			echo "$(RED)❌ 文件传输失败$(NC)"; \
			rm -f $(PROJECT_NAME)_$(TIMESTAMP).tar.gz; \
			exit 1; \
		fi; \
	fi
	@echo "$(GREEN)✅ 传输文件完整性验证通过$(NC)"
	@echo "$(YELLOW)正在服务器上导入镜像...$(NC)"
	@ssh $(SERVER_USER)@$(SERVER_HOST) " \
		cd /tmp && \
		echo '正在解压并导入镜像...' && \
		if ! gunzip -c $(PROJECT_NAME)_$(TIMESTAMP).tar.gz | docker load; then \
			echo '❌ Docker导入失败，尝试清理并重试...'; \
			docker system prune -f >/dev/null 2>&1 || true; \
			echo '正在重新尝试导入...'; \
			gunzip -c $(PROJECT_NAME)_$(TIMESTAMP).tar.gz | docker load || exit 1; \
		fi"
	@echo "$(YELLOW)正在更新latest标签...$(NC)"
	ssh $(SERVER_USER)@$(SERVER_HOST) "docker tag $(VERSION_TAG) $(PROJECT_NAME):latest"
	@echo "$(YELLOW)正在清理临时文件...$(NC)"
	ssh $(SERVER_USER)@$(SERVER_HOST) "rm -f /tmp/$(PROJECT_NAME)_$(TIMESTAMP).tar.gz"
	@echo "$(GREEN)镜像推送完成$(NC)"
	rm -f $(PROJECT_NAME)_$(TIMESTAMP).tar.gz
	rm -f $(BUILD_TIMESTAMP_FILE)

# 在服务器上管理版本（保持最新5个版本）
manage-versions:
	@echo "$(YELLOW)正在管理服务器版本...$(NC)"
	@ssh $(SERVER_USER)@$(SERVER_HOST) ' \
		echo "管理镜像版本..." && \
		IMAGES=$$(docker images $(PROJECT_NAME) --format "{{.Tag}}" | grep -E "^[0-9]{8}.[0-9]{6}$$" | sort -r) && \
		COUNT=$$(echo "$$IMAGES" | wc -l) && \
		if [ "$$COUNT" -gt 5 ]; then \
			echo "发现 $$COUNT 个版本，删除旧版本..." && \
			echo "$$IMAGES" | tail -n +6 | while read tag; do \
				echo "删除旧版本: $(PROJECT_NAME):$$tag" && \
				docker rmi $(PROJECT_NAME):$$tag 2>/dev/null || true; \
			done; \
		else \
			echo "当前版本数量: $$COUNT，无需清理"; \
		fi'

# 交互式部署应用
deploy:
	@echo "$(GREEN)ByrSki 应用层部署选择$(NC)"
	@echo "请选择部署方式:"
	@echo "  1) 重新构建并部署 (完整构建)"
	@echo "  2) 使用服务器现有镜像部署 (快速部署)"
	@echo ""
	@read -p "请输入选择 (1-2): " choice; \
	case $$choice in \
		1) echo "$(YELLOW)选择: 重新构建并部署$(NC)"; $(MAKE) deploy-rebuild ;; \
		2) echo "$(YELLOW)选择: 使用现有镜像部署$(NC)"; $(MAKE) deploy-existing ;; \
		*) echo "$(RED)无效选择，请输入 1 或 2$(NC)"; exit 1 ;; \
	esac

# 重新构建并部署应用
deploy-rebuild: build push manage-versions
	@echo "$(YELLOW)正在部署应用...$(NC)"
	@echo "$(YELLOW)部署版本: $(APP_VERSION) ($(DOCKER_TAG))$(NC)"
	@echo "$(YELLOW)确保环境变量配置...$(NC)"
	@if [ ! -f ".env" ]; then \
		echo "$(YELLOW)⚠️  未找到.env文件，请确保DATABASE_SERVER_IP已配置$(NC)"; \
	fi
	ssh $(SERVER_USER)@$(SERVER_HOST) "cd /root/$(PROJECT_NAME) && docker-compose down && docker-compose up -d"
	@echo "$(GREEN)应用部署完成！$(NC)"
	@echo "$(GREEN)版本信息:$(NC)"
	@echo "  应用版本: $(APP_VERSION)"
	@echo "  Docker标签: $(DOCKER_TAG)"
	@echo "  Git提交: $(GIT_COMMIT)"
	@echo "$(GREEN)访问地址:$(NC)"
	@echo "  生产实例: http://$(SERVER_HOST):8081"
	@echo "  蓝色实例: http://$(SERVER_HOST):8080"
	@echo "  绿色实例: http://$(SERVER_HOST):8082"

# 使用服务器现有镜像部署应用
deploy-existing:
	@echo "$(YELLOW)使用服务器现有镜像部署应用...$(NC)"
	@echo "#!/bin/bash" > deploy_tmp.sh
	@echo "if docker images $(PROJECT_NAME):latest --format '{{.Repository}}' | grep -q $(PROJECT_NAME); then" >> deploy_tmp.sh
	@echo "    echo '✅ 发现现有镜像: $(PROJECT_NAME):latest'" >> deploy_tmp.sh
	@echo "    cd /root/$(PROJECT_NAME) && docker-compose down && docker-compose up -d" >> deploy_tmp.sh
	@echo "    exit 0" >> deploy_tmp.sh
	@echo "else" >> deploy_tmp.sh
	@echo "    echo '❌ 服务器上未发现 $(PROJECT_NAME):latest 镜像'" >> deploy_tmp.sh
	@echo "    echo '请先运行 make deploy-rebuild 构建并上传镜像'" >> deploy_tmp.sh
	@echo "    exit 1" >> deploy_tmp.sh
	@echo "fi" >> deploy_tmp.sh

	@chmod +x deploy_tmp.sh
	@scp deploy_tmp.sh $(SERVER_USER)@$(SERVER_HOST):/tmp/
	@ssh $(SERVER_USER)@$(SERVER_HOST) "bash /tmp/deploy_tmp.sh"
	@ssh $(SERVER_USER)@$(SERVER_HOST) "rm -f /tmp/deploy_tmp.sh"
	@rm -f deploy_tmp.sh

	@echo "$(GREEN)应用部署完成！$(NC)"
	@echo "$(GREEN)使用现有镜像: $(PROJECT_NAME):latest$(NC)"
	@echo "$(GREEN)访问地址:$(NC)"
	@echo "  生产实例: http://$(SERVER_HOST):8081"
	@echo "  蓝色实例: http://$(SERVER_HOST):8080"
	@echo "  绿色实例: http://$(SERVER_HOST):8082"

# 显示版本信息
versions:
	@echo "$(GREEN)最近5个版本:$(NC)"
	@ssh $(SERVER_USER)@$(SERVER_HOST) ' \
		echo "服务器上的镜像版本:" && \
		docker images $(PROJECT_NAME) --format "table {{.Tag}}\t{{.CreatedAt}}\t{{.Size}}" | head -6 && \
		echo "" && \
		echo "当前运行的容器:" && \
		docker ps --filter "name=byrski" --format "table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}"'

# 回退到指定版本
rollback:
	@echo "$(YELLOW)可用版本列表:$(NC)"
	@ssh $(SERVER_USER)@$(SERVER_HOST) "docker images $(PROJECT_NAME) --format '{{.Tag}}' | grep -E '^[0-9]{8}_[0-9]{6}$$' | head -5 | nl"
	@echo ""
	@read -p "请输入要回退的版本号 (格式: YYYYMMDD_HHMMSS): " version; \
	if [ -z "$$version" ]; then \
		echo "$(RED)错误: 版本号不能为空$(NC)"; \
		exit 1; \
	fi; \
	echo "$(YELLOW)正在回退到版本: $$version$(NC)"; \
	ssh $(SERVER_USER)@$(SERVER_HOST) " \
		if docker images $(PROJECT_NAME):$$version --format '{{.Tag}}' | grep -q $$version; then \
			echo '标记版本 $$version 为 latest...' && \
			docker tag $(PROJECT_NAME):$$version $(PROJECT_NAME):latest && \
			cd /root/$(PROJECT_NAME) && \
			echo '重启应用...' && \
			docker-compose down && \
			docker-compose up -d && \
			echo '回退完成！'; \
		else \
			echo '错误: 版本 $$version 不存在'; \
			exit 1; \
		fi"; \
	if [ $$? -eq 0 ]; then \
		echo "$(GREEN)成功回退到版本: $$version$(NC)"; \
	else \
		echo "$(RED)回退失败$(NC)"; \
	fi

# 清理本地镜像
clean:
	@echo "$(YELLOW)清理本地镜像...$(NC)"
	docker images $(PROJECT_NAME) --format "{{.Tag}}" | grep -E "^[0-9]{8}_[0-9]{6}$$" | head -n +6 | xargs -r docker rmi $(PROJECT_NAME): 2>/dev/null || true
	@echo "$(YELLOW)清理构建时间戳文件...$(NC)"
	rm -f $(BUILD_TIMESTAMP_FILE)
	@echo "$(GREEN)清理完成$(NC)"

# 查看服务器日志
logs:
	@echo "$(YELLOW)查看production容器日志...$(NC)"
	ssh $(SERVER_USER)@$(SERVER_HOST) "cd /root/$(PROJECT_NAME) && docker-compose logs -f --tail=50 app-production"

# 监控服务状态
status:
	@echo "$(GREEN)应用层服务状态:$(NC)"
	@ssh $(SERVER_USER)@$(SERVER_HOST) "cd /root/$(PROJECT_NAME) && docker-compose ps"

# 检查Docker多平台支持
check-platform:
	@echo "$(GREEN)检查Docker多平台构建支持:$(NC)"
	@echo "当前平台: $(CURRENT_PLATFORM)"
	@echo "目标平台: $(TARGET_PLATFORM)"
	@echo ""
	@echo "检查Docker buildx支持:"
	@if docker buildx version >/dev/null 2>&1; then \
		echo "$(GREEN)✅ Docker buildx 可用$(NC)"; \
		docker buildx ls; \
	else \
		echo "$(RED)❌ Docker buildx 不可用$(NC)"; \
		echo "$(YELLOW)建议升级Docker到最新版本以获得更好的多平台支持$(NC)"; \
	fi
	@echo ""
	@echo "检查可用平台:"
	@docker buildx inspect --bootstrap 2>/dev/null | grep "Platforms:" || echo "$(YELLOW)无法获取平台信息$(NC)"

# 诊断应用服务器环境
diagnose:
	@echo "$(GREEN)ByrSki 应用服务器环境诊断$(NC)"
	@echo "=========================================="
	@echo ""
	@echo "$(YELLOW)1. 检查服务器连接...$(NC)"
	@if ssh $(SERVER_USER)@$(SERVER_HOST) "echo '✅ SSH连接正常'" 2>/dev/null; then \
		echo "$(GREEN)✅ 服务器连接正常$(NC)"; \
	else \
		echo "$(RED)❌ 无法连接到服务器$(NC)"; \
		echo "请检查:"; \
		echo "  - 服务器IP是否正确: $(SERVER_HOST)"; \
		echo "  - SSH密钥是否配置"; \
		echo "  - 网络连接是否正常"; \
		exit 1; \
	fi
	@echo ""
	@echo "$(YELLOW)2. 检查Docker服务...$(NC)"
	@ssh $(SERVER_USER)@$(SERVER_HOST) " \
		if docker --version >/dev/null 2>&1; then \
			echo '✅ Docker已安装: '\$$(docker --version); \
		else \
			echo '❌ Docker未安装或不可用'; \
			exit 1; \
		fi; \
		if docker info >/dev/null 2>&1; then \
			echo '✅ Docker服务运行正常'; \
		else \
			echo '❌ Docker服务未运行'; \
			echo '尝试启动Docker服务...'; \
			systemctl start docker 2>/dev/null || service docker start 2>/dev/null || echo '无法启动Docker服务'; \
		fi"
	@echo ""
	@echo "$(YELLOW)3. 检查磁盘空间...$(NC)"
	@ssh $(SERVER_USER)@$(SERVER_HOST) " \
		echo '根目录:'; \
		df -h / | tail -n 1; \
		echo '/tmp目录:'; \
		df -h /tmp | tail -n 1; \
		echo '/var/lib/docker目录:'; \
		df -h /var/lib/docker 2>/dev/null | tail -n 1 || echo '目录不存在或无权限'"
	@echo ""
	@echo "$(YELLOW)4. 检查Docker存储状态...$(NC)"
	@ssh $(SERVER_USER)@$(SERVER_HOST) " \
		if docker system df 2>/dev/null; then \
			echo ''; \
			echo 'Docker磁盘使用情况:'; \
			docker system df; \
		else \
			echo '❌ 无法获取Docker存储信息'; \
		fi"
	@echo ""
	@echo "$(YELLOW)5. 检查项目目录...$(NC)"
	@ssh $(SERVER_USER)@$(SERVER_HOST) " \
		if [ -d '/root/$(PROJECT_NAME)' ]; then \
			echo '✅ 项目目录存在: /root/$(PROJECT_NAME)'; \
			ls -la /root/$(PROJECT_NAME)/; \
		else \
			echo '⚠️  项目目录不存在，将创建: /root/$(PROJECT_NAME)'; \
			mkdir -p /root/$(PROJECT_NAME); \
		fi"
	@echo ""
	@echo "$(YELLOW)6. 检查运行中的容器...$(NC)"
	@ssh $(SERVER_USER)@$(SERVER_HOST) " \
		if docker ps -a --filter 'name=$(PROJECT_NAME)' --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}' | grep -q $(PROJECT_NAME); then \
			echo '发现相关容器:'; \
			docker ps -a --filter 'name=$(PROJECT_NAME)' --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'; \
		else \
			echo '⚠️  未发现相关容器'; \
		fi"
	@echo ""
	@echo "$(YELLOW)7. 检查镜像...$(NC)"
	@ssh $(SERVER_USER)@$(SERVER_HOST) " \
		if docker images $(PROJECT_NAME) --format 'table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}' | grep -q $(PROJECT_NAME); then \
			echo '发现项目镜像:'; \
			docker images $(PROJECT_NAME) --format 'table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}'; \
		else \
			echo '⚠️  未发现项目镜像'; \
		fi"
	@echo ""
	@echo "$(YELLOW)8. 检查环境配置...$(NC)"
	@ssh $(SERVER_USER)@$(SERVER_HOST) " \
		if [ -f '/root/$(PROJECT_NAME)/docker-compose.yml' ]; then \
			echo '✅ Docker Compose配置存在'; \
			echo '配置文件内容:'; \
			grep -E 'DATABASE_SERVER_IP|SPRING_DATA' /root/$(PROJECT_NAME)/docker-compose.yml || echo '无相关配置'; \
		else \
			echo '⚠️  Docker Compose配置不存在'; \
		fi"
	@echo ""
	@echo "$(GREEN)=========================================="
	@echo "诊断完成！$(NC)"

# 自动修复应用服务器Docker问题
fix-server:
	@echo "$(GREEN)ByrSki 应用服务器自动修复$(NC)"
	@echo "=========================================="
	@echo ""
	@echo "$(YELLOW)正在诊断问题...$(NC)"
	@$(MAKE) diagnose
	@echo ""
	@echo "$(YELLOW)开始自动修复...$(NC)"
	@echo ""
	@echo "$(YELLOW)1. 清理Docker系统...$(NC)"
	@ssh $(SERVER_USER)@$(SERVER_HOST) " \
		echo '清理未使用的容器...'; \
		docker container prune -f >/dev/null 2>&1 || true; \
		echo '清理未使用的镜像...'; \
		docker image prune -f >/dev/null 2>&1 || true; \
		echo '清理未使用的网络...'; \
		docker network prune -f >/dev/null 2>&1 || true; \
		echo '清理未使用的卷...'; \
		docker volume prune -f >/dev/null 2>&1 || true; \
		echo '清理构建缓存...'; \
		docker builder prune -f >/dev/null 2>&1 || true"
	@echo ""
	@echo "$(YELLOW)2. 重启Docker服务...$(NC)"
	@ssh $(SERVER_USER)@$(SERVER_HOST) " \
		echo '正在重启Docker服务...'; \
		systemctl restart docker 2>/dev/null || service docker restart 2>/dev/null || echo '无法重启Docker服务'; \
		sleep 3; \
		if docker info >/dev/null 2>&1; then \
			echo '✅ Docker服务重启成功'; \
		else \
			echo '❌ Docker服务重启失败'; \
			exit 1; \
		fi"
	@echo ""
	@echo "$(YELLOW)3. 清理临时文件...$(NC)"
	@ssh $(SERVER_USER)@$(SERVER_HOST) " \
		echo '清理/tmp目录下的旧文件...'; \
		find /tmp -name '$(PROJECT_NAME)_*.tar.gz' -mtime +1 -delete 2>/dev/null || true; \
		echo '清理Docker临时文件...'; \
		rm -rf /var/lib/docker/tmp/* 2>/dev/null || true"
	@echo ""
	@echo "$(YELLOW)4. 重新初始化项目环境...$(NC)"
	@$(MAKE) init-server
	@echo ""
	@echo "$(GREEN)=========================================="
	@echo "自动修复完成！$(NC)"

# 强制重新构建并部署
rebuild:
	@echo "$(GREEN)强制重新构建并部署$(NC)"
	@echo "=========================================="
	@echo ""
	@echo "$(YELLOW)清理本地环境...$(NC)"
	@$(MAKE) clean
	@echo ""
	@echo "$(YELLOW)清理服务器Docker环境...$(NC)"
	@ssh $(SERVER_USER)@$(SERVER_HOST) " \
		echo '停止相关容器...'; \
		docker-compose -f /root/$(PROJECT_NAME)/docker-compose.yml down 2>/dev/null || true; \
		echo '删除项目镜像...'; \
		docker images $(PROJECT_NAME) -q | xargs -r docker rmi -f 2>/dev/null || true; \
		echo '清理Docker系统...'; \
		docker system prune -f >/dev/null 2>&1 || true"
	@echo ""
	@echo "$(YELLOW)重新构建项目...$(NC)"
	@$(MAKE) build
	@echo ""
	@echo "$(YELLOW)重新部署服务...$(NC)"
	@$(MAKE) deploy-rebuild
	@echo ""
	@echo "$(GREEN)=========================================="
	@echo "强制重建完成！$(NC)"

# 初始化应用服务器环境
init-server:
	@echo "$(GREEN)初始化应用服务器环境...$(NC)"
	@echo "$(YELLOW)正在创建项目目录...$(NC)"
	ssh $(SERVER_USER)@$(SERVER_HOST) "mkdir -p /root/$(PROJECT_NAME)"
	@echo "$(YELLOW)正在检查Docker环境...$(NC)"
	ssh $(SERVER_USER)@$(SERVER_HOST) "docker --version && docker-compose --version"
	@echo "$(YELLOW)正在传输docker-compose配置...$(NC)"
	scp docker-compose-app.yml $(SERVER_USER)@$(SERVER_HOST):/root/$(PROJECT_NAME)/docker-compose.yml
	@echo "$(YELLOW)配置环境变量提醒...$(NC)"
	@echo "$(YELLOW)⚠️  请确保在服务器上配置正确的环境变量:$(NC)"
	@echo "  export DATABASE_SERVER_IP=<数据库服务器IP>"
	@echo "$(GREEN)应用服务器环境初始化完成！$(NC)"
	@echo "$(YELLOW)提示: 现在可以运行 'make deploy' 进行部署$(NC)"
