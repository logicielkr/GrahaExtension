# Graha Extension Library

## 0. notice

최근에 Graha 소스코드는 새롭게 작성되었고,
이를 반영하기 위한 약간의 수정이 있었다.

## 1. 암호화

### 1.1. AES-GCM([EncryptorAESGCMImpl.java](https://github.com/logicielkr/GrahaExtension/blob/main/src/kr/graha/app/encryptor/EncryptorAESGCMImpl.java))

AES-GCM 은 Java 에 내장된 혹은 [BouncyCastle](https://www.bouncycastle.org/) 을 사용하여 구현하였다.

> Java 7 혹은 그 이전 버전은 AES-GCM 을 지원하지 않으므로 [BouncyCastle](https://www.bouncycastle.org/) 이 필수이다.

공개된 pwd 값과 iv 값을 사용할 것이 아니라면, [EncryptorAESGCMImpl.java](https://github.com/logicielkr/GrahaExtension/blob/main/src/kr/graha/app/encryptor/EncryptorAESGCMImpl.java) 파일의 다음 부분을 수정한 이후에, 컴파일하여 사용해야 하며, 이에 대해서는 후술하는 컴파일 가이드 편을 참조한다.

```java
protected final String pwd = "change it!";
protected final String iv = "Change It!!!";
```

이 라이브러리는 [WebMUA](https://github.com/logicielkr/WebMUA)의 [account.xml](https://github.com/logicielkr/WebMUA/blob/main/WEB-INF/graha/account.xml), [FetchMailProcessorImpl.java](https://github.com/logicielkr/WebMUA/blob/main/src/kr/graha/sample/webmua/FetchMailProcessorImpl.java), MailSendProcessorImpl.java(https://github.com/logicielkr/WebMUA/blob/main/src/kr/graha/sample/webmua/MailSendProcessorImpl.java) 에서 사용되었다.

### 1.2. MD5([EncryptorMD5Impl.java](https://github.com/logicielkr/GrahaExtension/blob/main/src/kr/graha/app/encryptor/EncryptorMD5Impl.java)) 혹은 SHA512([EncryptorSha512Impl.java](https://github.com/logicielkr/GrahaExtension/blob/main/src/kr/graha/app/encryptor/EncryptorSha512Impl.java))

MD5 혹은 SHA512 은 Java 에 내장된 것을 사용하여 구현하였다.

```java
kr.graha.app.encryptor.EncryptorMD5Impl md5 = new kr.graha.app.encryptor.EncryptorMD5Impl();
String encrypted = md5.encrypt("plain text");
```

```java
kr.graha.app.encryptor.EncryptorSha512Impl  sha512 = new kr.graha.app.encryptor.EncryptorSha512Impl();
String encrypted = sha512.encrypt("plain text");
```

decrypt 메소드는 항상 null 을 반환한다.

## 2. 정렬순서 변경 ([ApplyOrderNumberProcessorImpl.java](https://github.com/logicielkr/GrahaExtension/blob/main/src/kr/graha/app/command/ApplyOrderNumberProcessorImpl.java))

[WebMUA](https://github.com/logicielkr/WebMUA) 의 계정 목록 화면에서 순서 변경 기능을 위해 개발했던 것을 범용적으로 사용할 수 있도록 하였다.

이 라이브러리를 사용하기 위해서는 
클라이언트 쪽에 [order_number.js](https://github.com/logicielkr/WebMUA/blob/main/js/order_number.js) 를 포함시켜야 하고,
서버 쪽에서도 이 라이브러리를 호출하는 코드를 추가해야 한다.

이에 대해서는 [WebMUA](https://github.com/logicielkr/WebMUA)의 [account.xml](https://github.com/logicielkr/WebMUA/blob/main/WEB-INF/graha/account.xml)을 참조한다.

## 3. 컴파일 가이드

Apache Ant 를 위한 build.xml (build.graha-extension.xml) 을 동봉하였다.

> Apache Ant 가 처음인 사람들은 필자가 작성한 [Apache Ant 가이드](https://logiciel.kr/graha/contents/detail.html?contents_id=3068) 를 참조한다.

컴파일을 위해 다음과 같은 .jar 파일이 있어야 한다.

- bcprov-jdk15to18-169.jar: [BouncyCastle](https://www.bouncycastle.org/)
- servlet-api.jar
- graha.jar

build.graha-extension.xml 에서 다음과 같은 항목을 다운로드 받은 jar 파일의 경로에 맞게 수정한다(오른쪽은 기본값이다).

- graha.lib.dir : /opt/java/lib/graha
- apache.tomcat.lib.dir : /opt/java/lib/apache-tomcat-7.0.100
- apache.tomcat10.lib.dir : /opt/java/lib/apache-tomcat-10.0.0-M1
- bouncy.castle.lib.dir : /opt/java/lib/bouncy_castle ([BouncyCastle](https://www.bouncycastle.org/) 은 버전에 따라 파일이름이 다를 것이므로 master-classpath 도 수정해야 한다)
- version.file : ${graha.lib.dir}/version.property ([version.property](https://github.com/logicielkr/graha/blob/master/version.property) 를 사용하거나, app.graha.version 를 설정할 수도 있다)

혹은 master-classpath 를 수정한다.
