## 简介

kronos在古希腊代表时间之神的意思，本项目解决了前后端时区不一致的问题，可以将用户的时区时间转为统一的服务端时区时间，接口响应时再按照用户的时区返回对应的时间。

## 贡献 | Contributing

- 李子园

# 使用方法

引入`kronos-spring-boot-starter`

```xml
       <dependency>
              <groupId>com.kronos</groupId>
              <artifactId>kronos-spring-boot-starter</artifactId>
              <version>1.0.0</version>
        </dependency>
```

使用@ConvertTime标记接口方法，支持类级别和方法级别，使用@Time标记需要时区转换的字段

```java
@RestController
@RequestMapping("/test")
public class TestController {

    @ConvertTime
    @GetMapping("/date")
    public ResponseEntity<String> getDate(@Time @RequestParam("date")@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime date) {
        System.out.println(date);
        return  ResponseEntity.ok("hello world");
    }

    @ConvertTime
    @PostMapping("/date")
    public ResponseEntity<String> postDate(@RequestBody Data date) {
        System.out.println(date);
        return  ResponseEntity.ok("hello world");
    }
}

@lombok.Data
public class Data {

    @Time
    private String date;
}
```

配置文件

```yaml
spring:
  kronos:
    backendZoneId: UTC  #后端时区
    defaultClientZoneId: Asia/Shanghai  #前端默认时区，没有请求头则使用默认时区
    clientZoneIdHeader: X-Time-Zone  # 默认前端传递时区的请求头
    defaultDateTimeFormat: yyyy-MM-dd HH:mm:ss  #默认的时间格式

```

