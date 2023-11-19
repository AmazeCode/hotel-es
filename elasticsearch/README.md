# elasticsearch-ik-plugin

#### 介绍 
docker-compose 搭建 Elstaticsearch 和 Kibana 及安装 Ik分词插件  (version:7.17.6) 

GitHub地址: https://github.com/kingsfeng/elasticsearch-ik-plugin

#### 软件架构
docker-compose 
Elstaticsearch
Kibana
Ik

### 安装及配置说明

#### elasticsearch.yml
#### 设置JVM堆内存初始大小
`-Xms2g`
#### 设置JVM堆内存最大大小
`-Xmx2g`
#### 建议将Elasticsearch的JVM堆内存大小设置为服务器物理内存的一半（50%）

#### deploy是一个部署选项，resources是资源限制和预留的配置。
#### limits：用于设置容器可以使用的资源上限。在这里，cpus: "2" 表示该容器可以使用的CPU核心数的上限为2个，memory: 1024M 表示容器可以使用的内存上限为1024兆字节（MB）。
#### reservations：用于设置容器资源的预留。预留是一种保证，在资源紧张的情况下，系统会为容器保留指定数量的资源。在这里，memory: 200M 表示容器将被预留至少200兆字节（MB）的内存，以确保即使在高负载时也有足够的内存供容器使用。

### IK 中文分词器安装
`https://github.com/medcl/elasticsearch-analysis-ik`
`https://github.com/medcl/elasticsearch-analysis-ik/releases/tag/v7.17.6`
#### 从GitHub下载分词器对应版本（与elasticsearch版本保持一致）解压缩后移动到 plugins


### 使用说明

#### 启动命令
`docker-compose up -d` 

#### 停止命令
`docker-compose stop `

#### 查看启动情况及容器ID
`docker ps`

#### 进入容器目录
`docker exec -it 容器ID /bin/bash`


