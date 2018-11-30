package com.company.project.generator;

import com.google.common.base.CaseFormat;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.*;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 代码生成器，根据数据表名称生成对应的Model、Mapper、Service、Controller简化开发。
 */
public class CodeGenerator {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/test";
    private static final String JDBC_USERNAME = "root";
    private static final String JDBC_PASSWORD = "123456";
    private static final String JDBC_DIVER_CLASS_NAME = "com.mysql.jdbc.Driver";

    private static final String PROJECT_ABSOLYTE_PATH =
            String.join(File.separator, System.getProperty("user.dir"), "spring-boot-api-project-seed");//项目在硬盘上的基础路径
    private static final String JAVA_PATH = "/src/main/java"; //java文件路径
    private static final String RESOURCES_PATH = "/src/main/resources";//资源文件路径
    private static final String BASE_PACKAGE =  "com.company.project";

    private static final String CONFIG_PATH = "generator";
    private static final String CONFIG_FILE = String.join("/", CONFIG_PATH, "gen.properties");
    private static final String TEMPLATE_FILE_PATH = String.join("/", CONFIG_PATH, "template");//模板位置
    private static final String AUTHOR = "CodeGenerator";//@author
    private static final String DATE = new SimpleDateFormat("yyyy/MM/dd").format(new Date());//@date

    private GeneratorConfig generatorConfig;

    public static void main(String[] args) {
        new CodeGenerator().genCode(args);
        //genCodeByCustomModelName("输入表名","输入自定义Model名称");/
    }

    public CodeGenerator() {
        this.generatorConfig = new GeneratorConfig().loadConfig(CONFIG_FILE);
    }

    private class GeneratorConfig {
        private String jdbcUrl = JDBC_URL;
        private String jdbcUserName = JDBC_USERNAME;
        private String jdbcPassword = JDBC_PASSWORD;
        private String jdbcDriverClassName = JDBC_DIVER_CLASS_NAME;

        private String projectAbsolutePath = PROJECT_ABSOLYTE_PATH;
        private String javaPath = JAVA_PATH;
        private String resourcePath = RESOURCES_PATH;
        private String basePackage = BASE_PACKAGE;

        private String[] tableNames = null;

        public String getJdbcUrl() {
            return jdbcUrl;
        }

        public String getJdbcUserName() {
            return jdbcUserName;
        }

        public String getJdbcPassword() {
            return jdbcPassword;
        }

        public String getJdbcDriverClassName() {
            return jdbcDriverClassName;
        }

        public String getProjectAbsolutePath() {
            return projectAbsolutePath;
        }

        public String getJavaPath() {
            return javaPath;
        }

        public String getJavaAbsolutePath() {
            return String.join("", projectAbsolutePath, javaPath);
        }

        public String getResourcePath() {
            return resourcePath;
        }

        public String getResourceAbsolutePath() {
            return String.join("", projectAbsolutePath, resourcePath);
        }

        public String getBasePackage() {
            return basePackage;
        }

        public String[] getTableNames() {
            return tableNames;
        }

        public GeneratorConfig loadConfig(String configFilePath) {
            try {
                Properties prop = new Properties();
                File configFile = new File(configFilePath);
                if (configFile.exists()) {
                    System.out.println(String.format("Load configuration %s...", configFilePath));
                    prop.load(new FileInputStream(configFile));
                } else {
                    String configResourceFile = String.join("", "/", configFilePath);
                    System.out.println(String.format("Load configuration from resource %s...", configResourceFile));
                    InputStream inputStream = CodeGenerator.class.getResourceAsStream(configResourceFile);
                    prop.load(inputStream);
                }
                String jdbcUrl = prop.getProperty("jdbcUrl");
                String jdbcUserName = prop.getProperty("jdbcUserName");
                String jdbcPassword = prop.getProperty("jdbcPassword");
                String jdbcDriverClassName = prop.getProperty("jdbcDriverClassName");

                String projectAbsolutePath = prop.getProperty("projectAbsolutePath");
                String javaPath = prop.getProperty("javaPath");
                String resourcePath = prop.getProperty("resourcePath");
                String basePackage = prop.getProperty("basePackage");

                String tableNamesStr = prop.getProperty("tableNames");

                if (StringUtils.isNotBlank(jdbcUrl)) {
                    this.jdbcUrl = jdbcUrl;
                }
                if (StringUtils.isNotBlank(jdbcUserName)) {
                    this.jdbcUserName = jdbcUserName;
                }
                if (StringUtils.isNotBlank(jdbcPassword)) {
                    this.jdbcPassword = jdbcPassword;
                }
                if (StringUtils.isNotBlank(jdbcDriverClassName)) {
                    this.jdbcDriverClassName = jdbcDriverClassName;
                }
                if (StringUtils.isNotBlank(projectAbsolutePath)) {
                    this.projectAbsolutePath = projectAbsolutePath;
                }
                if (StringUtils.isNotBlank(javaPath)) {
                    this.javaPath = javaPath;
                }
                if (StringUtils.isNotBlank(resourcePath)) {
                    this.resourcePath = resourcePath;
                }
                if (StringUtils.isNotBlank(basePackage)) {
                    this.basePackage = basePackage;
                }
                if (StringUtils.isNotBlank(tableNamesStr)) {
                    this.tableNames = tableNamesStr.trim().split("\\s*,\\s*");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println(String.format("projectAbsolutePath: %s", this.projectAbsolutePath));
            System.out.println(String.format("basePackage: %s", this.basePackage));
            System.out.println(String.format("tableNames: %s", String.join(",",  this.tableNames)));
            return this;
        }

        public String getModelPackage() {
            return String.join(".", basePackage, "model");
        }

        public String getMapperPackage() {
            return String.join(".", basePackage, "dao");
        }

        public String getServicePackage() {
            return String.join(".", basePackage, "service");
        }

        public String getServiceImplPackage() {
            return String.join(".", basePackage, "service.impl");
        }

        public String getControllerImplPackage() {
            return String.join(".", basePackage, "web");
        }

        public String getMapperInterfaceReference() {
            return String.join(".", basePackage, "core.Mapper");
        }

        //生成的Service存放路径
        public String getServicePackagePath() {
            return packageConvertPath(getServicePackage());
        }

        //生成的Service实现存放路径
        public String getServiceImplPackagePath() {
            return packageConvertPath(getServiceImplPackage());
        }

        //生成的Controller存放路径
        public String getControllerImplPackagePath() {
            return packageConvertPath(getControllerImplPackage());
        }

        private String packageConvertPath(String packageName) {
            return String.format("/%s/", packageName.contains(".") ? packageName.replaceAll("\\.", "/") : packageName);
        }

    }

    /**
     * 通过数据表名称生成代码，Model 名称通过解析数据表名称获得，下划线转大驼峰的形式。
     * 如输入表名称 "t_user_detail" 将生成 TUserDetail、TUserDetailMapper、TUserDetailService ...
     * @param tableNames 数据表名称...
     */
    public void genCode(String... tableNames) {
        String[] genTableNames = this.generatorConfig.getTableNames();
        if (tableNames != null && tableNames.length > 0) {
            genTableNames = tableNames;
        }
        for (String tableName : genTableNames) {
            genCodeByCustomModelName(tableName, null);
        }
    }

    /**
     * 通过数据表名称，和自定义的 Model 名称生成代码
     * 如输入表名称 "t_user_detail" 和自定义的 Model 名称 "User" 将生成 User、UserMapper、UserService ...
     * @param tableName 数据表名称
     * @param modelName 自定义的 Model 名称
     */
    public void genCodeByCustomModelName(String tableName, String modelName) {
        genModelAndMapper(tableName, modelName);
        genService(tableName, modelName);
        genController(tableName, modelName);
    }


    public void genModelAndMapper(String tableName, String modelName) {
        Context context = new Context(ModelType.FLAT);
        context.setId("Potato");
        context.setTargetRuntime("MyBatis3Simple");
        context.addProperty(PropertyRegistry.CONTEXT_BEGINNING_DELIMITER, "`");
        context.addProperty(PropertyRegistry.CONTEXT_ENDING_DELIMITER, "`");

        JDBCConnectionConfiguration jdbcConnectionConfiguration = new JDBCConnectionConfiguration();
        jdbcConnectionConfiguration.setConnectionURL(generatorConfig.getJdbcUrl());
        jdbcConnectionConfiguration.setUserId(generatorConfig.getJdbcUserName());
        jdbcConnectionConfiguration.setPassword(generatorConfig.getJdbcPassword());
        jdbcConnectionConfiguration.setDriverClass(generatorConfig.getJdbcDriverClassName());
        context.setJdbcConnectionConfiguration(jdbcConnectionConfiguration);

        PluginConfiguration pluginConfiguration = new PluginConfiguration();
        pluginConfiguration.setConfigurationType("tk.mybatis.mapper.generator.MapperPlugin");
        pluginConfiguration.addProperty("mappers", generatorConfig.getMapperInterfaceReference());
        context.addPluginConfiguration(pluginConfiguration);

        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();
        javaModelGeneratorConfiguration.setTargetProject(generatorConfig.getJavaAbsolutePath());
        javaModelGeneratorConfiguration.setTargetPackage(generatorConfig.getModelPackage());
        context.setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);

        SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration();
        sqlMapGeneratorConfiguration.setTargetProject(generatorConfig.getResourceAbsolutePath());
        sqlMapGeneratorConfiguration.setTargetPackage("mapper");
        context.setSqlMapGeneratorConfiguration(sqlMapGeneratorConfiguration);

        JavaClientGeneratorConfiguration javaClientGeneratorConfiguration = new JavaClientGeneratorConfiguration();
        javaClientGeneratorConfiguration.setTargetProject(generatorConfig.getJavaAbsolutePath());
        javaClientGeneratorConfiguration.setTargetPackage(generatorConfig.getMapperPackage());
        javaClientGeneratorConfiguration.setConfigurationType("XMLMAPPER");
        context.setJavaClientGeneratorConfiguration(javaClientGeneratorConfiguration);

        TableConfiguration tableConfiguration = new TableConfiguration(context);
        tableConfiguration.setTableName(tableName);
        if (StringUtils.isNotEmpty(modelName))tableConfiguration.setDomainObjectName(modelName);
        tableConfiguration.setGeneratedKey(new GeneratedKey("id", "Mysql", true, null));
        context.addTableConfiguration(tableConfiguration);

        List<String> warnings;
        MyBatisGenerator generator;
        try {
            Configuration config = new Configuration();
            config.addContext(context);
            config.validate();

            boolean overwrite = true;
            DefaultShellCallback callback = new DefaultShellCallback(overwrite);
            warnings = new ArrayList<String>();
            generator = new MyBatisGenerator(config, callback, warnings);
            generator.generate(null);
        } catch (Exception e) {
            throw new RuntimeException("generate Model and Mapper failed", e);
        }

        if (generator.getGeneratedJavaFiles().isEmpty() || generator.getGeneratedXmlFiles().isEmpty()) {
            throw new RuntimeException("generate Model and Mapper failed:" + warnings);
        }
        if (StringUtils.isEmpty(modelName)) modelName = tableNameConvertUpperCamel(tableName);
        System.out.println(modelName + ".java generated");
        System.out.println(modelName + "Mapper.java generated");
        System.out.println(modelName + "Mapper.xml generated");
    }


    public void genService(String tableName, String modelName) {
        try {
            freemarker.template.Configuration cfg = getConfiguration();

            Map<String, Object> data = new HashMap<>();
            data.put("date", DATE);
            data.put("author", AUTHOR);
            String modelNameUpperCamel = StringUtils.isEmpty(modelName) ? tableNameConvertUpperCamel(tableName) : modelName;
            data.put("modelNameUpperCamel", modelNameUpperCamel);
            data.put("modelNameLowerCamel", tableNameConvertLowerCamel(tableName));
            data.put("basePackage", generatorConfig.getBasePackage());


            File file = new File(getServiceFilePath(modelNameUpperCamel));
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            cfg.getTemplate("service.ftl").process(data,
                    new FileWriter(file));
            System.out.println(modelNameUpperCamel + "Service.java generated");

            File file1 = new File(getServiceImplFilePath(modelNameUpperCamel));
            if (!file1.getParentFile().exists()) {
                file1.getParentFile().mkdirs();
            }
            cfg.getTemplate("service-impl.ftl").process(data,
                    new FileWriter(file1));
            System.out.println(modelNameUpperCamel + "ServiceImpl.java generated");
        } catch (Exception e) {
            throw new RuntimeException("generate Service failed", e);
        }
    }

    public void genController(String tableName, String modelName) {
        try {
            freemarker.template.Configuration cfg = getConfiguration();

            Map<String, Object> data = new HashMap<>();
            data.put("date", DATE);
            data.put("author", AUTHOR);
            String modelNameUpperCamel = StringUtils.isEmpty(modelName) ? tableNameConvertUpperCamel(tableName) : modelName;
            data.put("baseRequestMapping", modelNameConvertMappingPath(modelNameUpperCamel));
            data.put("modelNameUpperCamel", modelNameUpperCamel);
            data.put("modelNameLowerCamel", CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, modelNameUpperCamel));
            data.put("basePackage", generatorConfig.getBasePackage());

            File file = new File(getControllerFilePath(modelNameUpperCamel));
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            cfg.getTemplate("controller-restful.ftl").process(data, new FileWriter(file));
//            cfg.getTemplate("controller.ftl").process(data, new FileWriter(file));

            System.out.println(modelNameUpperCamel + "Controller.java generated");
        } catch (Exception e) {
            throw new RuntimeException("generate Controller failed", e);
        }

    }

    private freemarker.template.Configuration getConfiguration() throws IOException {
        freemarker.template.Configuration cfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_23);
        File templateDir = new File(TEMPLATE_FILE_PATH);
        if (templateDir.exists()) {
            cfg.setDirectoryForTemplateLoading(templateDir);
        } else {
            String templateDirResource = String.join("", "/", TEMPLATE_FILE_PATH);
            cfg.setClassForTemplateLoading(this.getClass(), templateDirResource);
        }

        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
        return cfg;
    }

    private String tableNameConvertLowerCamel(String tableName) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, tableName.toLowerCase());
    }

    private String tableNameConvertUpperCamel(String tableName) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName.toLowerCase());

    }

    private String tableNameConvertMappingPath(String tableName) {
        tableName = tableName.toLowerCase();//兼容使用大写的表名
        return "/" + (tableName.contains("_") ? tableName.replaceAll("_", "/") : tableName);
    }

    private String modelNameConvertMappingPath(String modelName) {
        String tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, modelName);
        return tableNameConvertMappingPath(tableName);
    }

    private String getServiceFilePath(String modelName) {
        return String.join("", generatorConfig.getJavaAbsolutePath(),
                generatorConfig.getServicePackagePath(), modelName, "Service.java");
    }

    private String getServiceImplFilePath(String modelName) {
        return String.join("", generatorConfig.getJavaAbsolutePath(),
                generatorConfig.getServiceImplPackagePath(), modelName, "ServiceImpl.java");
    }

    private String getControllerFilePath(String modelName) {
        return String.join("", generatorConfig.getJavaAbsolutePath(),
                generatorConfig.getControllerImplPackagePath(), modelName, "Controller.java");
    }
}
