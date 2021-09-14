# stars-datachange

#### 介绍
**开源数据转换工具，强大的可插拔、非侵入式设计，两种数据源转换结构，可以优雅的使你的数据字典转换为你想要的结果。

#### 软件架构
基于springboot、mybatis

#### 安装教程

开箱即用：导入**数据转换依赖

``` bash
<!-- https://mvnrepository.com/artifact/com.gitee.xuan_zheng/stars-datachange -->
<dependency>
    <groupId>com.gitee.xuan_zheng</groupId>
    <artifactId>stars-datachange</artifactId>
    <version>1.0.2</version>
</dependency>
```

#### 使用说明

``` bash
1. 定义数据字典枚举，demo可在依赖包中找到，如下图：
```
![数据字典枚举](https://images.gitee.com/uploads/images/2021/0914/162348_91a9b26a_5384206.png "数据字典枚举")

``` bash
2. 标记要使用数据转换功能的数据模型，如下图：
```
![数据模型](https://images.gitee.com/uploads/images/2021/0914/162728_692d2d18_5384206.png "屏幕截图.png")

``` bash
3. 执行
```
![数据转换](https://images.gitee.com/uploads/images/2021/0914/163139_e15ee7d8_5384206.png "屏幕截图.png")

#### 参与贡献

1.  感谢 [SpringBootStarter](https://github.com/SpringBootStarter/) 提出的建设性见解


#### 反馈交流

- QQ交流群：322536109