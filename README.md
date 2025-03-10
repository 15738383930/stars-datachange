# stars-datachange
![stars-datachange](src/main/resources/logo.jpg)

#### 介绍
**开源数据对比、转换工具，强大的可插拔、非侵入式设计，两种数据转换结构，如女朋友般优雅、丝滑。

#### 软件架构
基于springboot、springboot-web、mybatis、lombok

#### 安装教程

导入**数据转换依赖即可

``` bash
<!-- https://mvnrepository.com/artifact/com.gitee.xuan_zheng/stars-datachange -->
<dependency>
    <groupId>com.gitee.xuan_zheng</groupId>
    <artifactId>stars-datachange</artifactId>
    <version>2.0.0</version>
</dependency>
```
[GitHub传送门](https://github.com/15738383930/stars-datachange)

#### Demo Project
[Demo project url](https://gitee.com/xuan_zheng/data-change)

#### 使用说明（方式一：数据字典枚举）

``` bash
1. 定义数据字典枚举，demo可在依赖包中找到，如下图：
```
![数据字典枚举](https://images.gitee.com/uploads/images/2021/0914/162348_91a9b26a_5384206.png "数据字典枚举.png")

``` bash
2. 标记要使用数据转换功能的数据模型，如下图：
```
![数据模型](https://images.gitee.com/uploads/images/2021/0929/162008_fe6f6e6b_5384206.png "数据模型.png")

``` bash
3. 执行
```
![数据转换](https://images.gitee.com/uploads/images/2021/0914/163139_e15ee7d8_5384206.png "数据转换.png")

#### 使用说明（方式二：动态数据字典）

ps:动态数据字典模式，需要你的项目连接数据库。

``` bash
1. 找到你的字典表(没有请参考下图创建)，如下图：
ps：图中字段都可自定义（详见步骤二），这里按图中字段讲解：
    1.1 code——对应属性的字典代码
    1.2 name——对应属性的字典值（也就是你要的结果）
    1.3 type——对应属性的名称（下图中，第一行数据是后几行数据的父级，用parent_id声明【parent_id、id等字段都可自定义】）
```
![字典表](https://images.gitee.com/uploads/images/2021/0914/185007_7dff798a_5384206.png "字典表.png")

``` bash
2. 在配置文件中加入数据字典配置（图中几个属性分别对应表名和表中字段），如下图：
```
![数据字典配置](https://images.gitee.com/uploads/images/2021/0914/185321_4d8e2880_5384206.png "数据字典配置.png")

``` bash
3. 在配置文件中追加mapper-locations配置【classpath*:mapper/*.xml】（多个配置以逗号分隔），如下图：
```
![mapper-locations配置](https://images.gitee.com/uploads/images/2021/0915/113547_bb67813b_5384206.png "mapper-locations配置.png")

``` bash
4. 标记要使用动态数据转换功能的数据模型，如下图：
ps： 需要切换到DB源模式，modelName——数据模型名称（值默认为首字母小写的数据模型名称），对应字典表中父字典的列——type的值（建议用数据模型的名称，首字母小写）
```
![标记数据模型](https://images.gitee.com/uploads/images/2021/0929/161824_deed0579_5384206.png "标记数据模型.png")

``` bash
5. 执行
```
![数据转换](https://images.gitee.com/uploads/images/2021/0914/163139_e15ee7d8_5384206.png "数据转换.png")


#### 两种使用方式区别
``` bash
1.  字典枚举：配置步骤少，定义的字典枚举可作为常量进行条件判断；字典值是静态的，不易维护
```
![字典枚举作为常量](https://images.gitee.com/uploads/images/2021/0918/094747_7f1bccda_5384206.png "字典枚举作为常量.png")
``` bash
2.  数据字典：可以动态维护数据字典；配置步骤略多，需要字典进行条件判断时，需要定义常量
```

#### 参与贡献

1.  感谢 [SpringBootStarter](https://github.com/SpringBootStarter/) 提出的建设性见解
2.  感谢 [Speciallei](https://gitee.com/speciallei) 提供的logo原型
3.  感谢  **xuhb20**  支持了￥20：找了好久，非常感谢！
> _再次感谢大家的支持！_ 

#### 反馈交流

- QQ交流群：322536109


#### 版本更新说明

- v2.0：重大升级 [v2.0](https://blog.csdn.net/qq_36206259/article/details/145636230)
- v1.8：添加了数据转换的重入功能 [v1.8特色](https://blog.csdn.net/qq_36206259/article/details/142638600)
- v1.7：添加了属性别名配置；添加了默认数据字典的全表查询逻辑；mapper配置文件目录更换 [v1.7特色](https://blog.csdn.net/qq_36206259/article/details/140540848)
- v1.6.0：添加了数据转换反转功能；添加了非数据库项目环境下的使用配置 [v1.6.0特色](https://blog.csdn.net/qq_36206259/article/details/129553987)
- v1.5.0：修复了springboot依赖漏洞；（重点）添加了动态数据字典自定义功能 [v1.5.0特色](https://blog.csdn.net/qq_36206259/article/details/126193341)
- v1.4.0：明确了项目特色：如女朋友般优雅、丝滑；添加了项目启动banner；优化了n次代码结构，看上去更加丝滑~ [v1.4.0特色](https://blog.csdn.net/qq_36206259/article/details/126161820)
- v1.3.0：优化了代码结构；减少了开发者的编码量；定义了字典枚举的规范；添加了自定义数据转换（k-v互转）[v1.3.0新特性](https://blog.csdn.net/qq_36206259/article/details/125021976)
- v1.2.0：由v1.1.2变更为v1.2.0（较大升级）[v1.2.0新特性](https://blog.csdn.net/qq_36206259/article/details/124515865)
- v1.1.2：修复了数据转换时，通用父类导致的数据转换异常；添加了属性映射模式的数据转换功能。
- v1.1.1：注解兼容模块优化；
- v1.1.0：添加了自定义注解兼容模块(详见：[DataChangeCompatibleConfig.java](https://gitee.com/xuan_zheng/data-change))；
- v1.0.x：完成了基本数据转换、数据对比等功能；