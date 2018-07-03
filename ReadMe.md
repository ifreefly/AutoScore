# 使用指导
使用指导参见UserGuide.md

# 编译环境
windows 7/10 ，JDK 8  

如果是其他环境，可能需要修改pom文件的相关配置

# 编译安装方法
```
mvn clean install
```

# 其他
1. 项目运行需要使用JDK tools.jar，项目已经预先打包
2. 编译如果存在问题，请修改pom文件中对tools.jar的相关依赖
