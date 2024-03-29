# 자바 가상 머신(Java Virtual Machine)   

<br>

## JVM이란
1. 자바 프로그램이 어느 기기, 어느 운영체제 상에서도 실행될 수 있게 하는 것   
2. 프로그램 메모리를 관리하고 최적화하는 것   

<br>

## 역할   
- 시스템 메모리를 관리하면서 자바 기반 애플리케이션을 위해 이식 가능한 실행 환경을 제공한다.   
- JAVA와 OS 사이에서 중개자 역할을 하여 어느 운영체제 상에서도 실행될 수 있게 해준다.   

<br>

## 동작 방식
![image](https://github.com/yuriyeom/TIL/assets/43941336/af31b60d-fd98-4de1-be51-119f2d958ce4)
1. 자바 프로그램을 실행하면 JVM은 OS로부터 메모리를 할당받는다.
2. 자바 컴파일러(javac)가 자바 소스코드(.java)를 자바 바이트 코드(.class)로 컴파일한다.
3. Class Loader는 동적 로딩을 통해 필요한 클래스들을 로딩 및 링크하여 Runtime Data Area(실질적인 메모리를 할당받아 관리하는 영역)에 올린다.
4. Runtime Data Area에 로딩된 바이트 코드(.class)는 Execution Engine을 통해 해석된다.
5. 이 과정에서 Execution Engine에 의해 Garbage Collector의 작동과 Thread 동기화가 이루어진다.

<br>

## 구조   
![image](https://github.com/yuriyeom/TIL/assets/43941336/0c06b19d-d6b4-4977-b28b-181ddb5dfed3)

<br>

### 1. Class Loader
```
JVM 내로 class 파일을 동적으로 로드하고 Link를 통해 적절히 배치하는 일련의 작업을 수행하는 모듈. 
로드된 바이트 코드들을 엮어서 JVM의 메모리 영역인 Runtime Data Area에 배치한다. 
한 번에 메모리에 올리지 않고, 어플리케이션에서 필요한 경우(참조되는 순간) 동적으로 메모리에 적재하게 된다.
```
로딩(Loading) → 링킹(Linking) → 초기화(Initialization)
Loading	: 클래스 파일을 JVM의 메모리에 탑재하는 과정
Linking	: 클래스 파일을 사용하기 위해 검증하는 과정
Initialization : static field의 값들을 정의한 값으로 초기화를 하는 과정

<br>

### 2. Runtime Data Area   

```
JVM이라는 프로그램이 운영체제 위에서 실행되면서 할당받는 메모리 영역
```

![image](https://github.com/yuriyeom/TIL/assets/43941336/28599b58-fd31-41d6-933d-afdd3dc93177)
메서드, 힙 : 모든 스레드가 공유해서 사용 (GC의 대상)   
스택, PC 레지스터, 네이티브 메서드 스택 : 각 스레드(Thread) 마다 하나씩 생성   


1. **메서드 영역**
    
    JVM이 시작될 때 생성되고, JVM이 읽은 각각의 클래스와 인터페이스에 대한 런타임 상수 풀, 필드 및 메서드 코드, 정적 변수, 메서드의 바이트 코드 등을 보관함
    
2. **힙 영역**
    
    런타임에 동적으로 할당되는 데이터가 저장되는 영역. 객체나 배열 생성 등 Reference Type이 여기에 해당함
    
    ![image](https://github.com/yuriyeom/TIL/assets/43941336/9ea9b86f-8aa8-4af6-90d1-852384278bc8)

    
    - **New/Young Generation** : Java 객체가 생성되자마자 저장되고, 생긴지 얼마 안되는 객체가 저장되는 공간. java 객체가 생성되면 이 영역에 저장되다가, 시간이 지남에 따라 우선순위가 낮아지면 Old영역으로 옮겨진다. 이 영역에서 사라질 때 Minor GC가 발생한다.
        - **Eden** : new를 통해 새로 생성된 객체가 위치. 정기적인 쓰레기 수집 후 살아남은 객체들은 Survivor로 이동
        - **Survivor 0 / Survivor 1** : 각 영역이 채워지게 되면, 살아남은 객체는 비워진 Survivor로 순차적으로 이동
    - **Old(Tenured) Generation** : Young Generation 영역에서 저장되었던 객체 중에서 오래된 객체가 이동되어서 저장되는 영역, 이 영역에서 객체가 사라질 때 Major GC(Full GC)가 발생한다.
        - Old영역에 할당된 메모리가 허용치를 넘게 되면, Old 영역에 있는 모든 객체들을 검사하여 참조되지 않는 객체들을 한꺼번에 삭제하는 GC가 실행된다. 시간이 오래 걸리는 작업이고 이 때 GC를 실행하는 쓰레드를 제외한 모든 스레드는 작업을 멈추게 된다. 이를 'Stop-the-World' 라 한다.
    - **Permanent Generation** : Class loader에 의해 load되는 Class, Method 등에 대한 Meta 정보가 저장되는 영역으로 JVM에 의하여 사용된다. Reflection을 사용하여 동적으로 클래스가 로딩되는 경우에 사용된다. Java 8부터 제거됐다.
3. **스택 영역**
    
    지역변수, 매개변수, 메소드 정보, 임시 데이터 등을 저장
    
    메소드 호출 시마다 각각의 스택프레임(메서드만을 위한 공간)이 생성된다. 
    
    메서드 수행이 끝나면 프레임 별로 삭제를 한다.
    
    스택 사이즈가 고정되어 있어, 런타임 시에 스택 사이즈를 바꿀 수 없다. 프로그램 실행 중 메모리 크기가 충분하지 않다면 StackOverFlowError 발생
    
4. **PC 레지스터**
    
    현재 수행중인 JVM 명령어 주소를 저장하고 있는 영역
    
5. **네이티브 메서드 스택**
    
    실제 실행할 수 있는 기계어로 작성된 프로그램을 실행시키는 영역
    
    Java 이외의 언어로 만들어진 코드들을 위한 Stack.
    
    JNI(Java Native Interface)를 통해 호출되는 C / C++ 등의 코드를 수행하기 위한 Stack.
    
    JVM 내부에 영향을 주지 않기 위해 따로 메모리 공간을 활용한다.

<br>

### 3. Execution Engine   

```
Class Loader를 통해 JVM 내의 런타임 데이터 영역에 배치된 바이트 코드는 실행 엔진에 의해 실행. 
실행 엔진은 바이트 코드를 명령어 단위로 읽어서 실행
```
자바 바이트 코드(*.class)는 기계가 바로 수행할 수 있는 언어보다는 가상머신이 이해할 수 있는 중간 레벨로 컴파일 된 코드이다. 그래서 실행 엔진은 이와 같은 바이트 코드를 실제로 JVM 내부에서 기계가 실행할 수 있는 형태로 변경해준다.

<br>

**인터프리터**와 **JIT 컴파일러** 두 가지 방식을 혼합하여 바이트 코드를 실행

<br>

**인터프리터**

바이트 코드 명령어를 하나씩 읽어서 해석하고 바로 실행한다.

JVM안에서 바이트코드는 기본적으로 인터프리터 방식으로 동작한다.

다만 같은 메소드 라도 여러번 호출이 된다면 매번 해석하고 수행해야 되서 전체적인 속도는 느리다.

<br>

**JIT 컴파일러**

인터프리터의 단점을 보완하기 위해 도입한 방식.

반복되는 코드를 발견하여 바이트 코드 전체를 컴파일하여 네이티브 코드로 변경하고 이후에는 해당 메서드를 더 이상 인터프리팅하지 않고 캐싱해두었다가 네이티브 코드로 직접 실행한다.

JIT는 ByteCode를 어셈블러 같은 NativeCode로 바꿔서 실행이 빠르지만 역시 변환하는데 비용이 발생했고, 이 같은 이유 때문에 JVM은 모든 코드를 JIT Compiler 방식으로 실행하지 않고 Interpreter 방식을 사용하다 일정한 기준이 넘어가면 JIT Compiler 방식으로 실행한다.
