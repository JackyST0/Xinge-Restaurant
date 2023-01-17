Java项目实践--新哥点餐
---
### 1.新哥点餐项目介绍
* #### 项目介绍：
  - 本项目（新哥点餐）是专门为餐饮企业（餐厅、饭店）定制的一款软件产品，包括系统管理后台和移动端应用两部分。其中系统管理后台主要提供给餐饮企业内部员工使用，可以对餐厅的菜品、套餐、订单等进行管理维护。而移动端应用主要提供给消费者使用，可以在线浏览菜品、添加购物车、下单等。
* #### 本项目主要分为三期进行开发：
  - 第一期主要是针对系统管理后台实现基本需求，如员工登录、添加员工、添加菜品、修改菜品、添加套餐、删除套餐、查询订单等。
  - 第二期主要针对移动端应用进行改进，添加了用户手机验证码登录功能、购物车功能、新增地址功能、下单功能、订单查询功能等。
  - 第三期主要针对整个项目进行优化升级，提高系统的访问性能，例如使用Redis获取手机验证码、利用缓存存取数据、实现数据库的读写分离、通过Swagger编写接口文档等。
* #### 本项目需要掌握的一些技术栈：
  - 后端：
    - 对Maven工程的使用需要有一定的了解。
    - 对Spring、Spring MVC、Spring Session有一定的掌握。
    - 对Spring Boot开发框架以及注解的使用非常熟练。
    - 对Mysql数据库联合各表间的CRUD要熟练运用，对Mysql数据库的读写分离、主从复制有一定的认识，后面项目优化会用到。
    - 对Spring Boot整合Mybatis、Mybatis Plus对数据库进行CRUD的操作要熟练，特别是要会用构造器和Lambda表达式。
    - 对Redis缓存有了解，知道其能实现项目中的哪些功能，后面项目优化会用到。
  - 前端：
    - 对前端三剑客要熟练，代码的大概要能看懂。
    - 对vue框架能看懂，如何通过ajax发请求，发送的请求地址是多少。
    - 对Element-UI要有一定的掌握，起码知道代码之间怎么调用。
    - 对网站会通过F12调出控制台自行查看请求和应答、报错也会通过控制台进行调错。

### 2.新哥点餐项目过程
* #### 第一期重点部分：
    - ##### 完善登录功能
        ```
        /**
        * 检查用户是否已经完成登录
        */
        @WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
        @Slf4j
        public class LoginCheckFilter implements Filter {

            //路径匹配器，支持通配符
            public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
                    throws IOException, ServletException {
                HttpServletRequest request=(HttpServletRequest) servletRequest;
                HttpServletResponse response=(HttpServletResponse) servletResponse;

                //1.获取本次请求的URI
                String requestURI=request.getRequestURI();

                log.info("拦截到请求：{}",requestURI);

                //定义不需要处理的请求路径
                String[] urls=new String[]{
                        "/employee/login",
                        "/employee/logout",
                        "/backend/**",
                        "/front/**",
                        "/common/**",
                        "/user/sendMsg", //移动端发送短信
                        "/user/login", //移动端登录
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources",
                        "/v2/api-docs"
                };

                //2.判断本次请求是否需要处理
                boolean check =check(urls,requestURI);

                //3.如果不需要处理，则直接放行
                if (check){
                    log.info("本次请求{}不需要处理",requestURI);
                    filterChain.doFilter(request,response);
                    return;
                }

                //4-1.判断管理端登录状态，如果已登录，则直接放行
                if(request.getSession().getAttribute("employee")!=null){
                    log.info("用户已登录，用户id为{}",request.getSession().getAttribute("employee"));

                    Long empId = (Long) request.getSession().getAttribute("employee");
                    BaseContext.setCurrentId(empId);

                    filterChain.doFilter(request,response);
                    return;
                }

                //4-2.判断移动端登录状态，如果已登录，则直接放行
                if(request.getSession().getAttribute("user")!=null){
                    log.info("用户已登录，用户id为{}",request.getSession().getAttribute("user"));

                    Long UserId = (Long) request.getSession().getAttribute("user");
                    BaseContext.setCurrentId(UserId);

                    filterChain.doFilter(request,response);
                    return;
                }

                log.info("用户未登录");
                //5.如果未登陆则返回未登录状态，通过输出流方式向客户端页面响应数据
                response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
                return;
            }

            /**
            * 路径匹配，检查本次请求是否需要放行
            * @param urls
            * @param requestURI
            * @return
            */
            public boolean check(String[] urls,String requestURI){
                for (String url : urls) {
                    boolean match=PATH_MATCHER.match(url,requestURI);
                    if (match){
                        return true;
                    }
                }
                return false;
            }
        }
        ```
        此功能通过使用自定义的过滤器来判断访问前台或者后台页面时用户或员工是否已经完成登录，如果没有登录则自动跳转到登录页面。通过过滤器获取到请求的URL与自定义不需要被处理的请求路径做比较，匹配则放行，不匹配则判断是否登录，是则调用filterChain对象的doFilter方法实施放行，否则就返回错误数据给前端，由前端接收数据再执行跳转操作到登录页面。
    
    - ##### 异常处理器 
        ``` 
        /**
        * 全局异常处理
        */
        @ControllerAdvice(annotations = {RestController.class, Controller.class})
        @ResponseBody
        @Slf4j
        public class GlobalExceptionHandler {

            /**
            * 异常处理方法捕获新增员工时账号重复异常
            * @return
            */
            @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
            public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
                log.error(ex.getMessage());
                if (ex.getMessage().contains("Duplicate entry")){
                    String[] split = ex.getMessage().split(" ");
                    String msg = split[2]+"已存在";
                    return R.error(msg);
                }
                return R.error("未知错误");
            }

            /**
            * 异常处理方法捕获删除菜品异常
            * @return
            */
            @ExceptionHandler(CustomException.class)
            public R<String> exceptionHandler(CustomException ex){
                log.error(ex.getMessage());

                return R.error(ex.getMessage());
            }
        }
        ```
        ```
        /**
        * 自定义业务异常类
        */
        public class CustomException extends RuntimeException{
            public CustomException(String message){
                super(message);
            }
        }
        ```
        此自定义的异常处理器主要是用来解决程序进行中一些异常的捕获，可以对一些被动抛出的异常进行处理，也可以自己在适当的时候主动抛出相关的异常来进行处理。就比如在新增员工时，当新增员工输入的账号已经存在，由于员工表中对该字段加入了唯一约束，此时程序就会抛异常，所以就需要一个Handler来捕获这个异常；又比如在删除菜品时，当删除菜品中有正在售卖的菜品而无法删除，如果硬要执行该操作，我们就可以使程序抛出一个异常，进而就可以通过异常处理器来捕获并处理这个异常。
    - ##### 员工信息分页查询
        ```
        @GetMapping("/page")
        public R<Page> page(int page,int pageSize,String name){
                log.info("page={},pageSize={},name={}",page,pageSize,name);
                //构造分页构造器
                Page pageInfo = new Page(page,pageSize);

                //构造条件构造器
                LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
                //添加过滤条件
                queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
                //添加排序条件
                queryWrapper.orderByDesc(Employee::getUpdateTime);

                //执行查询
                employeeService.page(pageInfo,queryWrapper);
                return R.success(pageInfo);
            }
        ```
        此代码通过访问页面发送ajax请求，将分页查询参数（page、pageSize、name）提交到服务端，然后服务端Controller接受页面提交的数据并调用Service查询数据，此处运用到了Mybatis Plus的技术，把分页构造器pageInfo和条件构造器queryWrapper作为参数传入IService的page方法中从而查询到分页数据，最后Controller将查询到的分页数据响应给前端，前端接收到分页数据并通过Element-UI的Table组件展示到页面上。
    - ##### 对象转换器
        ```
        /**
        * 对象映射器:基于jackson将Java对象转为json，或者将json转为Java对象
        * 将JSON解析为Java对象的过程称为 [从JSON反序列化Java对象]
        * 从Java对象生成JSON的过程称为 [序列化Java对象到JSON]
        */
        public class JacksonObjectMapper extends ObjectMapper {

            public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
            public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
            public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

            public JacksonObjectMapper() {
                super();
                //收到未知属性时不报异常
                this.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

                //反序列化时，属性不存在的兼容处理
                this.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);


                SimpleModule simpleModule = new SimpleModule()
                        .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                        .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                        .addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)))

                        .addSerializer(BigInteger.class, ToStringSerializer.instance)
                        .addSerializer(Long.class, ToStringSerializer.instance) //将Long型数据转为字符串以解决使用JS在前端页面显示JSON数据导致精度丢失问题
                        .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                        .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                        .addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));

                //注册功能模块 例如，可以添加自定义序列化器和反序列化器
                this.registerModule(simpleModule);
            }
        }
        ```
        ```
        @Slf4j
        @Configuration
        @EnableSwagger2
        @EnableKnife4j
        public class WebMvcConfig extends WebMvcConfigurationSupport {

            /**
            * 设置静态资源映射
            * @param registry
            */
            @Override
            protected void addResourceHandlers(ResourceHandlerRegistry registry) {
                log.info("开始进行静态资源映射...");
                registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
                registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
                registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
                registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
            }

            /**
            * 扩展mvc框架的消息转换器
            * @param converters
            */
            @Override
            protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
                log.info("扩展消息转换器...");
                //创建消息转换器对象
                MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
                //设置对象转换器，底层使用Jackson将Java对象转为json
                messageConverter.setObjectMapper(new JacksonObjectMapper());
                //将上面的消息转换器对象追加到mvc框架的转换器集合中
                converters.add(0,messageConverter);
            }
        }
        ```
        此自定义的对象转换器主要是解决前端页面js处理long型数字精度丢失的问题，由于js处理long型数字只能精确到前16位，所以通过ajax发送请求提交给服务端的id就会变，进而导致提交的id和数据库中的id不一致。因此就可以使用此转换器，首先是创建了这个对象转换器JacksonobjectMapper，基于Jackson进行Java对象到json数据的转换，然后在WebMvcConfig配置类中扩展Spring mvc的消息转换器，在此消息转换器中使用提供的对象转换器进行Java对象到json数据的转换，最后在服务端给前端返回json数据时进行处理，将Long型数据统一转为String字符串类型即可解决丢失精度的问题。
    - ##### 公共字段自动填充
        ```
        /**
        * 自定义元数据对象处理器
        */
        @Component
        @Slf4j
        public class MyMetaObjectHandler implements MetaObjectHandler {
            /**
            * 插入操作，自动填充
            * @param metaObject
            */
            @Override
            public void insertFill(MetaObject metaObject) {
                log.info("公共字段自动填充[insert]...");
                log.info(metaObject.toString());
                metaObject.setValue("createTime", LocalDateTime.now());
                metaObject.setValue("updateTime", LocalDateTime.now());
                metaObject.setValue("createUser", BaseContext.getCurrentId());
                metaObject.setValue("updateUser", BaseContext.getCurrentId());
            }

            /**
            * 更新操作，自动填充
            * @param metaObject
            */
            @Override
            public void updateFill(MetaObject metaObject) {
                log.info("公共字段自动填充[update]...");
                log.info(metaObject.toString());

                long id = Thread.currentThread().getId();
                log.info("线程id为：{}",id);

                metaObject.setValue("updateTime", LocalDateTime.now());
                metaObject.setValue("updateUser", BaseContext.getCurrentId());
            }
        }
        ```
        此自定义的对象处理器主要是解决在编辑员工时需要一一来设置修改时间和修改人等公共字段的问题，所以就可以通过Mybatis Plus提供的公共字段自动填充功能来实现统一处理这些公共字段的问题。首先Mybatis Plus公共字段自动填充，也就是在插入或者更新的时候为指定字段赋予指定的值，使用它的好处就是可以统一对这些字段进行处理，避免了重复代码。然后此类需要实现MetaObjectHandler接口再调用方法中的metaObject对象来进行统一的赋值，最后要在实体类的属性上加上@TableField注解，指定自动填充的策略方可统一来处理这些公共字段了。
    - ##### ThreadLocal封装工具类
        ```
        /**
        * 基于ThreadLocal封装工具类，用于保存和获取当前登录用户id
        */
        public class BaseContext {
            private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

            /**
            * 设置值
            * @param id
            */
            public static void setCurrentId(Long id){
                threadLocal.set(id);
            }

            /**
            * 获取值
            * @return
            */
            public static Long getCurrentId(){
                return threadLocal.get();
            }
        }
        ```
        此工具类主要是完善动态获取当前登录用户的id的功能，因为在上面我们完成的公共字段填充功能在自动填充createUser和updateUser时设置的用户id是固定值，现在我们需要改造成动态获取当前登录用户的id。（注意，我们在MyMetaObjectHandler类中是不能获得HttpSession对象的，所以我们需要通过其他方式来获取登录用户id。）所以我们使用了ThreadLocal来解决此问题，首先在上面的过滤器LoginCheckFilter的doFilter方法中获取当前登录用户id，并调用ThreadLocal的set方法来设置当前线程的线程局部变量的值（用户id)，然后在MyMetaObjectHandler的方法中调用ThreadLocal的get方法来获得当前线程所对应的线程局部变量的值(用户id)，这样createUser和updateUser就能动态填充上所需要的值了。
    - ##### 文件上传和下载
        ```
        /**
        * 文件上传和下载
        */
        @RequestMapping("/common")
        @RestController
        @Slf4j
        public class CommonController {

            @Value("${reggie.path}")
            private String basePath;

            /**
            * 文件上传
            * @param file
            * @return
            */
            @PostMapping("/upload")
            public R<String> upload(MultipartFile file){
                //file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除
                log.info(file.toString());

                //原始文件名
                String originalFilename = file.getOriginalFilename();
                String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

                //使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
                String fileName = UUID.randomUUID().toString() + suffix;

                //创建一个目录对象
                File dir = new File(basePath);
                //判断当前目录是否存在
                if (!dir.exists()){
                    //目录不存在，需要创建
                    dir.mkdirs();
                }

                try {
                    //将临时文件转存到指定位置
                    file.transferTo(new File(basePath + fileName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return R.success(fileName);
            }

            /**
            * 文件下载
            * @param name
            * @param response
            */
            @GetMapping("/download")
            public void download(String name, HttpServletResponse response){

                try {
                    //输入流，通过输入流读取文件内容
                    FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

                    //输出流，通过输出流将文件写回浏览器，在浏览器显示图片
                    ServletOutputStream outputStream = response.getOutputStream();

                    //响应回去什么类型的文件
                    response.setContentType("image/jpeg");

                    int len = 0;
                    byte[] bytes = new byte[1024];
                    while ((len = fileInputStream.read(bytes)) != -1){
                        outputStream.write(bytes,0,len);
                        outputStream.flush();
                    }

                    //关闭资源
                    outputStream.close();
                    fileInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        ```
        此控制器主要实现了文件上传和下载的功能，文件上传通过前端Element-UI提供的上传组件发送的请求，服务端接收到请求然后使用一个MultipartFile类型的参数即可接收到上传的文件，随后创建一个目录对象将临时文件转存到指定位置，返回文件名即表示上传成功。而文件下载则是将文件从服务端以流的形式写回浏览器的过程，这里主要是分别使用了输入流FileInputStream来读取文件内容和输出流 FileInputStream来将文件写回浏览器，最后得以在浏览器中显示图片。
    - ##### 新增套餐功能
        ```
        @Service
        public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService{

            @Autowired
            private SetmealDishService setmealDishService;

            //新增套餐，同时要保持与菜品的关联关系
            @Override
            @Transactional
            public void saveWithDish(SetmealDto setmealDto) {
                //保存套餐基本信息，操作setmeal，执行insert操作
                this.save(setmealDto);

                List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

                setmealDishes.stream().map((item)->{
                    item.setSetmealId(setmealDto.getId());
                    return item;
                }).collect(Collectors.toList());

                //保存套餐和菜品的关联信息，操作setmeal_dish，执行insert操作
                setmealDishService.saveBatch(setmealDishes);

            }
        }
        ```
        此代码主要是实现了新增套餐的功能，首先是前端页面发送请求，将套餐相关数据以json形式提交到服务端，然后服务端Controller接收到请求后调用setmealService中自定义的saveWithDish方法，最后把数据保存到数据库中，返回成功提示给前端即整个流程实现完毕。此功能实现的难点除了在于要熟练调用Mybatis Plus中Service的每个方法来执行数据库的操作，更在于在新增套餐的同时，要保持与菜品的关联关系，这里就通过了stream流以及运用了Lambda表达式的处理方法重新对列表setmealDishes中的每个菜品数据设置了套餐Id，使其保证了套餐与菜品之间的关联关系。

* #### 第二期重点部分：
    - ##### 新增地址和设置默认地址功能
        ```
        /**
        * 地址簿管理
        */
        @Slf4j
        @RestController
        @RequestMapping("/addressBook")
        public class AddressBookController {

            @Autowired
            private AddressBookService addressBookService;

            /**
            * 新增
            */
            @PostMapping
            public R<AddressBook> save(@RequestBody AddressBook addressBook) {
                addressBook.setUserId(BaseContext.getCurrentId());
                log.info("addressBook:{}", addressBook);
                addressBookService.save(addressBook);
                return R.success(addressBook);
            }

            /**
            * 设置默认地址
            */
            @PutMapping("default")
            public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
                log.info("addressBook:{}", addressBook);
                LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
                wrapper.set(AddressBook::getIsDefault, 0);
                //SQL:update address_book set is_default = 0 where user_id = ?
                addressBookService.update(wrapper);

                addressBook.setIsDefault(1);
                //SQL:update address_book set is_default = 1 where id = ?
                addressBookService.updateById(addressBook);
                return R.success(addressBook);
            }
        }
        ```
        此代码主要实现的是新增地址和把地址设置成默认地址的功能，首先新增地址是先利用工具类BaseContext的getCurrentId方法得到用户Id并赋值给AddressBook实体类对象，再调用Mybatis Plus中Service的save方法把该对象相关数据存进数据库，最后返回该对象回前端即新增完毕。而设置默认地址功能，其实就是先获取用户Id，把现用户所有地址的默认地址属性改为0，再通过前端发送的请求得到要设置默认地址的地址信息，随后把其地址的默认地址属性设为1并把数据返回给前端，此功能也实现完毕。
    - ##### 购物车功能
        ```
        @Slf4j
        @RestController
        @RequestMapping("/shoppingCart")
        public class ShoppingCartController {

            @Autowired
            private ShoppingCartService shoppingCartService;

            /**
            * 添加购物车
            * @param shoppingCart
            * @return
            */
            @PostMapping("/add")
            public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
                log.info("购物车数据：{}",shoppingCart);

                //设置用户id，指定当前是哪个用户的购物车数据
                Long currentId = BaseContext.getCurrentId();
                shoppingCart.setUserId(currentId);

                Long dishId = shoppingCart.getDishId();

                LambdaQueryWrapper<ShoppingCart> queryWrapper =new LambdaQueryWrapper<>();
                queryWrapper.eq(ShoppingCart::getUserId,currentId);

                if (dishId != null){
                    //添加到购物车的是菜品
                    queryWrapper.eq(ShoppingCart::getDishId,dishId);
                }else{
                    //添加到购物车的是套餐
                    queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
                }

                //查询当前菜品或者套餐是否在购物车中
                //SQL:select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
                ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

                if (cartServiceOne != null){
                    //如果已经存在，就在原来数量基础上加一
                    Integer number = cartServiceOne.getNumber();
                    cartServiceOne.setNumber(number + 1);
                    shoppingCartService.updateById(cartServiceOne);
                }else {
                    //如果不存在，则添加到购物车，数量默认就是一
                    shoppingCart.setNumber(1);
                    shoppingCart.setCreateTime(LocalDateTime.now());
                    shoppingCartService.save(shoppingCart);
                    cartServiceOne = shoppingCart;
                }

                return R.success(cartServiceOne);
            }
        }
        ```
        此代码主要实现的是移动端添加购物车的功能，首先依然是先利用工具类BaseContext的getCurrentId方法得到用户Id并设置给ShoppingCart实体类对象，指定当前是哪个用户的购物车，然后通过调用该对象中getDishId()的方法来判断添加到购物车的是菜品还是套餐，随后再通过调用Mybatis Plus中Service的getOne方法查询当前菜品或套餐是否在购物车中，如果存在则在原来数量的基础上加1，如果不存在，则新增到购物车中，数量默认为1，最后返回该购物车信息即完成此功能。
    - ##### 下单功能
        ```
        @Service
        public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

            @Autowired
            private ShoppingCartService shoppingCartService;

            @Autowired
            private UserService userService;

            @Autowired
            private AddressBookService addressBookService;

            @Autowired
            private OrderDetailService orderDetailService;

            @Autowired
            private OrderService orderService;

            /**
            * 用户下单
            * @param orders
            */
            @Transactional
            public void submit(Orders orders) {
                //获得当前用户id
                Long userId = BaseContext.getCurrentId();

                //查询当前用户的购物车数据
                LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ShoppingCart::getUserId,userId);
                List<ShoppingCart> shoppingCarts = shoppingCartService.list(wrapper);

                if (shoppingCarts == null || shoppingCarts.size() == 0){
                    throw new CustomException("购物车为空，不能下单");
                }

                //查询用户数据
                User user = userService.getById(userId);

                //查询地址数据
                Long addressBookId = orders.getAddressBookId();
                AddressBook addressBook = addressBookService.getById(addressBookId);
                if (addressBook == null){
                    throw new CustomException("用户地址信息有误，不能下单");
                }

                Long orderId = IdWorker.getId();//订单号

                AtomicInteger amount = new AtomicInteger(0);

                List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
                    OrderDetail orderDetail = new OrderDetail();//订单明细实体
                    orderDetail.setOrderId(orderId);//订单号
                    orderDetail.setNumber(item.getNumber());//菜品或套餐的份数
                    orderDetail.setDishFlavor(item.getDishFlavor());//菜品对应的口味
                    orderDetail.setDishId(item.getDishId());//菜品id
                    orderDetail.setSetmealId(item.getSetmealId());//套餐id
                    orderDetail.setName(item.getName());//菜品或套餐名称
                    orderDetail.setImage(item.getImage());//图片名称
                    orderDetail.setAmount(item.getAmount());//单份金额
                    amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
                    return orderDetail;
                }).collect(Collectors.toList());

                orders.setId(orderId);//订单号
                orders.setOrderTime(LocalDateTime.now());//下单时间
                orders.setCheckoutTime(LocalDateTime.now());//支付时间
                orders.setStatus(2);//订单状状态：待派送
                orders.setAmount(new BigDecimal(amount.get()));//总金额
                orders.setUserId(userId);//用户id
                orders.setNumber(String.valueOf(orderId));//订单号
                orders.setUserName(user.getName());//用户名称
                orders.setConsignee(addressBook.getConsignee());//收货人
                orders.setPhone(addressBook.getPhone());//手机号
                orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                        + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                        + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                        + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));//详细地址

                //向订单表插入数据，一条数据
                this.save(orders);

                //向订单明细表插入数据，多条数据
                orderDetailService.saveBatch(orderDetails);

                //清空购物车数据
                shoppingCartService.remove(wrapper);
            }
        }
        ```
        此代码主要实现的是用户移动端下单的功能，首先是获取用户Id，用来查询当前用户的购物车数据（这里创建了一个条件构造器再去调用list方法来查询得到数据），然后再查询用户数据和地址数据（这里就直接调用了相对应的Service方法拿到相关数据），随后使用了Mybatis Plus提供的IdWorker这个类的getId方法生成一个唯一订单号，再利用Stream流的形式和Lambda表达式给每一个OrderDetail对象逐一赋值，接着通过不同的set方法完善了orders对象的属性，最后再调用save方法和saveBatch方法把订单表和订单明细表全部向数据库插入数据再清空购物车的数据，此功能即实现完毕。
    - ##### 再来一单功能
        ```
        public void againSubmit(Map<String, String> map) {
                String ids = map.get("id");

                long id = Long.parseLong(ids);

                LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(OrderDetail::getOrderId,id);
                //获取该订单对应的所有的订单明细表
                List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);

                //通过用户id把原来的购物车给清空，这里的clean方法是视频中讲过的,建议抽取到service中,那么这里就可以直接调用了
                shoppingCartService.clean();

                //获取用户id
                Long userId = BaseContext.getCurrentId();
                List<ShoppingCart> shoppingCartList = orderDetailList.stream().map((item) -> {
                    //把从order表中和order_details表中获取到的数据赋值给这个购物车对象
                    ShoppingCart shoppingCart = new ShoppingCart();
                    shoppingCart.setUserId(userId);
                    shoppingCart.setImage(item.getImage());
                    Long dishId = item.getDishId();
                    Long setmealId = item.getSetmealId();
                    if (dishId != null) {
                        //如果是菜品那就添加菜品的查询条件
                        shoppingCart.setDishId(dishId);
                        shoppingCart.setDishFlavor(item.getDishFlavor());
                    } else {
                        //添加到购物车的是套餐
                        shoppingCart.setSetmealId(setmealId);
                    }
                    shoppingCart.setName(item.getName());
                    shoppingCart.setNumber(item.getNumber());
                    shoppingCart.setAmount(item.getAmount());
                    shoppingCart.setCreateTime(LocalDateTime.now());
                    return shoppingCart;
                }).collect(Collectors.toList());

                //把携带数据的购物车批量插入购物车表  这个批量保存的方法要使用熟练！！！
                shoppingCartService.saveBatch(shoppingCartList);
            }
        ```
        此代码主要实现的是用户移动端下完单后想要再来一单的功能，首先是通过传递过来的参数来获取到订单Id，再通过Id获取该订单对应的所有的订单明细表数据，然后这里记得要使用一下clean方法把用户Id原来的购物车清空（这个方法是在service中自己定义的，实现的是清空购物车的功能），随后获取用户Id依旧是通过流的方法把用户Id，order表、order_detail表中获取到的数据统一赋值给这个购物车对象，注意这里要判断一下重新下单的是菜品还是套餐，最后再调用saveBatch批量保存方法把携带数据的购物车对象批量插入到购物车当中即完成了此再来一单的功能。

* #### 第三期重点部分：
    - ##### 缓存短信验证码功能
        ```
        @Slf4j
        @RestController
        @RequestMapping("/user")
        public class UserController {

            @Autowired
            private UserService userService;

            @Autowired
            private RedisTemplate redisTemplate;

            /**
            * 发送手机短信验证码
            * @param user
            * @return
            */
            @PostMapping("/sendMsg")
            public R<String> sendMsg(@RequestBody User user, HttpSession session){
                //获取手机号
                String phone = user.getPhone();

                if (StringUtils.isNotEmpty(phone)){
                    //随机生成的4位验证码
                    String code = ValidateCodeUtils.generateValidateCode(4).toString();
                    log.info("code={}",code);

                    //需要将生成的验证码保存到Session
                    //session.setAttribute(phone,code);

                    //将生成的验证码缓存到Redis中，并且设置有效期为5分钟
                    redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);

                    return R.success("手机短信验证码发送成功");
                }

                return R.error("短信发送失败");
            }
        }
        ```
        此代码主要是实现用户移动端发送手机验证码请求的功能，这部分就开始涉及到简单的Redis用法了，首先通过前端发送的请求拿到数据获取手机号，其次调用自定义工具类ValidateCodeUtils的generateValidateCode方法来随机生成4位数验证码，然后再使用Spring Boot整合Redis中的RedisTemplate对象来操作缓存，调用该对象中的opsForValue方法利用Redis中String的存储数据类型把手机号作为Key、生成的验证码作为Value缓存到Redis中，并且设置TTL为5分钟，最后返回成功消息给前端即功能实现完毕。
    - ##### 缓存套餐数据
        ```
        /**
        * 根据条件查询套餐数据
        * @param setmeal
        * @return
        */
        @GetMapping("/list")
        @Cacheable(value = "setmealCache",key = "#setmeal.categoryId + '_list'")
        @ApiOperation(value = "套餐条件查询接口")
        public R<List<Setmeal>> list(Setmeal setmeal){
            LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
            queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
            queryWrapper.orderByDesc(Setmeal::getUpdateTime);

            List<Setmeal> list = setmealService.list(queryWrapper);

            return R.success(list);
            }
        ```
        此代码主要实现的是把套餐数据缓存到Redis当中，以减轻用户端每次发送请求都要去访问数据库而造成的负担的功能，这部分要用到Spring Cache这个框架的技术，它实现了可以基于注解的缓存功能简便了开发。首先记得要在启动类上加上@EnableCaching注解开启缓存注解功能，然后就可以直接在Controller的方法上加入注解，比如此代码实现的是缓存套餐数据功能，就可以在该list方法上使用@Cacheable注解，以达到在方法执行前spring先查看缓存中是否有数据，如果有数据，则直接返回缓存数据；若没有数据，调用方法并将方法返回值放到缓存中的目的。（注意：在使用缓存过程中，要注意保证数据库中的数据和缓存中的数据一致，如果数据库中的数据发生变化，需要及时清理缓存数据。）
    - ##### 读写分离
        ```
        spring:
          shardingsphere:
            datasource:
              names:
                master,slave
              # 主数据源
              master:
                type: com.alibaba.druid.pool.DruidDataSource
                driver-class-name: com.mysql.cj.jdbc.Driver
                url: jdbc:mysql://home.i-have-2-cats.xyz:38006/reggie?characterEncoding=utf-8
                username: root
                password: 123456
              # 从数据源
              slave:
                type: com.alibaba.druid.pool.DruidDataSource
                driver-class-name: com.mysql.cj.jdbc.Driver
                url: jdbc:mysql://home.i-have-2-cats.xyz:38004/reggie?characterEncoding=utf-8
                username: root
                password: root
            masterslave:
              # 读写分离配置
              load-balance-algorithm-type: round_robin
              # 最终的数据源名称
              name: dataSource
              # 主库数据源名称
              master-data-source-name: master
              # 从库数据源名称列表，多个逗号分隔
              slave-data-source-names: slave
            props:
              sql:
                show: true #开启SQL显示，默认false
          main:
            allow-bean-definition-overriding: true
        ```
        此配置文件是实现了Mysql数据库读写分离的功能，将数据库拆分为主库和从库，主库主要负责处理事务性的增删改写的操作，从库则负责处理查询读的操作，使得整个系统的查询性能得到极大的改善。而实现这一功能首先得创建两个数据库，将一台设为主库master，另一台设为从库slave，然后master将改变记录到二进制日志（ binary log)，slave将master的binary log拷贝到它的中继日志（relay log），slave重做中继日志中的事件，将改变应用到自己的数据库中，即实现了数据库之间主从复制的功能。这样一来，既能保证各个数据库之间数据的一致性，又可以提高每个数据库运行时进行操作的性能。（注意：在最后一行记得在配置文件中配置允许bean定义覆盖配置项）
    - ##### Swagger
        ```
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-spring-boot-starter</artifactId>
            <version>3.0.2</version>
        </dependency>
        ```
        ```
        @Slf4j
        @Configuration
        @EnableSwagger2
        @EnableKnife4j
        public class WebMvcConfig extends WebMvcConfigurationSupport {
        @Bean
        public Docket createRestApi() {
            //文档类型
            return new Docket(DocumentationType.SWAGGER_2)
                    .apiInfo(apiInfo())
                    .select()
                    .apis(RequestHandlerSelectors.basePackage("com.ka.reggie.controller"))
                    .paths(PathSelectors.any())
                    .build();
        }
        private ApiInfo apiInfo() {
            return new ApiInfoBuilder()
                    .title("瑞吉外卖")
                    .version("1.0")
                    .description("瑞吉外卖接口文档")
                    .build();
        }
        }
        ```
        ```
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        ```
        ```
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login",

                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"
        };
        ```
        ```
        /**
        * 套餐
        */
        @Data
        @ApiModel("套餐")
        public class Setmeal implements Serializable {

            private static final long serialVersionUID = 1L;

            @ApiModelProperty("主键")
            private Long id;


            //分类id
            @ApiModelProperty("分类id")
            private Long categoryId;


            //套餐名称
            @ApiModelProperty("套餐名称")
            private String name;


            //套餐价格
            @ApiModelProperty("套餐价格")
            private BigDecimal price;


            //状态 0:停用 1:启用
            @ApiModelProperty("状态")
            private Integer status;


            //编码
            @ApiModelProperty("套餐编号")
            private String code;


            //描述信息
            @ApiModelProperty("描述信息")
            private String description;


            //图片
            @ApiModelProperty("图片")
            private String image;


            @TableField(fill = FieldFill.INSERT)
            private LocalDateTime createTime;


            @TableField(fill = FieldFill.INSERT_UPDATE)
            private LocalDateTime updateTime;


            @TableField(fill = FieldFill.INSERT)
            private Long createUser;


            @TableField(fill = FieldFill.INSERT_UPDATE)
            private Long updateUser;


            //是否删除
            private Integer isDeleted;
        }
        ```
        这几段代码主要实现的是通过Swagger技术可以来查看接口文档，以及可以在线调试接口的功能。这部分涉及到了Swagger这个工具，使用Swagger你只需要按照它的规范去定义接口及接口相关的信息，再通过Swagger衍生出来的一系列项目和工具，就可以做到生成各种格式的接口文档，以及在线接口调试页面等等。首先，knife4j是为Java MVC框架集成Swagger生成Api文档的增强解决方案，所以得先导入knife4j的maven坐标，再编写knife4j相关的配置类并设置好静态资源映射，然后记得要在过滤器中设置不需要处理的请求路径便于正常访问，最后就是在每个请求类、请求方法，实体类、实体属性上加上相应的说明注解（@Api，@ApiModel，@ApiModelProperty，@ApiOperation，@ApilmplicitParams，ApilmplicitParam），随后访问对应的网页即可看到该项目所有接口的相关信息了。

### 3. 新哥点餐项目总结
- 这个项目给初入茅庐的后端开发程序员练手，我觉得是非常合适的，其结合当下较流行的后端开发框架Spring Boot，采用了标准后端的MVC模型进行开发，并整合了Mybatis Plus、Redis、Mysql、Swagger、Maven等各项热门技术，是刚入门的新手小白学习完各项技术后用来练习巩固知识的不二之选。

- 我也是一点点边跟着B站视频学习边敲代码，对项目中每个知识点都反复推敲琢磨，理清前端和后端是如何进行对接交互的，然后思考前端主要是做了什么工作（初始化数据，发请求，接受应答，渲染页面等等），思考后端主要又做了什么工作（接受请求，调用方法，对数据库的CRUD，返回应答等等），使我在理解和运用各项技术上面更加熟练。

- ##### 总的来说，跟着视频码就对了！！！
---
参考视频：[新哥点餐](https://www.bilibili.com/video/BV13a411q753?p=1&vd_source=a6541efd5c43d30c410c9e45054c9b89)
