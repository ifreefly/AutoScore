# 工程目录结构如下:
<pre>
    ├───autoScore-source-0.1.zip
    ├───start.bat
    │
    ├─bin
    │  ├───autoScore-0.1.jar
    │  │
    │  └─libs
    │
    ├─conf
    │  ├───log4j2.xml
    │  ├───scoreConfig.xml
    │  │
    │  └─template
    │      └──build.xml
    │
    ├─input
    ├─logs
    ├─output
    ├─tmp
    └─usecase
</pre>
<ul>
	<li>
	    autoScore-source-0.1.zip 自动阅卷工程源文件，如果程序不满足要求，可自行修改源码
	</li>
	<li>
	    start.bat 阅卷程序启动脚本
	</li>
	<li>
	    bin/autoScore-0.1.jar 阅卷程序
	</li>
	<li>
	    bin/libs 阅卷程序所依赖的lib库
	</li>
    <li>
	    conf/log4j2.xml 阅卷程序日志配置文件
	</li>
    <li>
	    conf/scoreConfig.xml 阅卷程序依赖的评分配置文件
	</li>
    <li>
	    template/build.xml 阅卷程序模版文件，用于ant构建使用，请勿随意修改！
	</li>
    <li>
	    input 试卷放在这，仅支持zip试卷
	</li>
    <li>
	    logs 日志目录
	</li>
    <li>
	    output 阅卷结果目录
	</li>
    <li>
	    tmp 阅卷临时工作目录
	</li>
    <li>
	    usecase 考试用例放在这，将整个test目录放在这。
	</li>
</ul>

# 使用指导
1. 需要配置环境变量JAVA_HOME，指向的路径需要是JDK所在的路径！
2. 删掉工程中的示例阅卷，将所有试卷文件放到input目录下，仅支持zip文件
3. 删掉工程中的示例用例，将所有考试用例放到usecase目录下
4. 修改conf/scoreConfig.xml 用例的分数权重，配置文件含义见后文描述
5. 双击start.bat即可开始阅卷
6. 进入output目录查看阅卷结果，结果描述见后文

# scoreConfig.xml
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<scores>
    <score className="idevcod.AppTest" method="test001" score="1" />
    <score className="idevcod.AppTest" method="test002" score="1" />
    <score className="idevcod.AppTest" method="test003" score="1" />
</scores>
```

className: 填写测试用例的类名，需要包含包名  
methos: 测试用例的方法名  
score: 测试用例的分值  

# output阅卷结果
output下的目录是以时间命名的，进入后，目录结构如下
<pre>
├─run
│  ├─caseResult
│  ├─runlog
│  └─score
└─summary
    ├─runSummary.log
    └─summary.csv
</pre>

<ul>
    <li>
        caseResult 原始的阅卷结果
    </li>
    <li>
        runlog 原始的ant运行结果
    </li>
    <li>
        score 考生的得分情况
    </li>
    <li>
        runSummary.log 总体阅卷情况，包含阅卷成功与失败，含阅卷进度打印
    </li>
    <li>
        summary.csv 得分情况汇总
    </li>
</ul>

注：如果csv文件使用excel打开乱码，请使用notepad++将文件格式转换为"以UTF-8模式编码", 不要选择无bom方式！