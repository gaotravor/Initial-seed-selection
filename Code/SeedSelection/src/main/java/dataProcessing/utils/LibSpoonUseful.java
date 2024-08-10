package dataProcessing.utils;

import spoon.Launcher;
import spoon.processing.AbstractProcessor;
import spoon.processing.ProcessingManager;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.DefaultCoreFactory;
import spoon.support.QueueProcessingManager;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

import java.io.File;
import java.util.*;

public class LibSpoonUseful {
    final Launcher launcher = new Launcher();
    Factory factory;

    LibSpoonUseful(String[] args){
        getFactoryWithLauncher(args);//只传入环境信息
    }

    /**
     *
     * @param srcPath 项目源文件目录
     * @param dependenciesPath 解析项目需要的依赖包
     * @param complianceLevel 项目编译级别
     * @param continueWhileBuildFailure build失败时是否继续读取解析成功的类
     */
    public LibSpoonUseful(String srcPath, String[] dependenciesPath, Integer complianceLevel, Boolean continueWhileBuildFailure) {
        buildModel(srcPath, dependenciesPath, complianceLevel, continueWhileBuildFailure);
        System.out.println("Number of CtTypes created: " + factory.Type().getAll().size());
    }

    private void buildModel(String srcPath, String[] dependenciesPath, Integer complianceLevel, Boolean continueWhileBuildFailure) {
        JDTBasedSpoonCompiler jdtBasedSpoonCompiler = null;
        factory = getFactory(complianceLevel);
        factory.getEnvironment().setCommentEnabled(false);
        factory.getEnvironment().setNoClasspath(false);
        factory.getEnvironment().setPreserveLineNumbers(false);
        factory.getEnvironment().setIgnoreDuplicateDeclarations(true);
        jdtBasedSpoonCompiler = new JDTBasedSpoonCompiler(factory);

        String[] sources = srcPath.split(File.pathSeparator);
        for (String src :sources) {
            if (!src.trim().isEmpty())
                jdtBasedSpoonCompiler.addInputSource(new File(src));
        }
        if (dependenciesPath != null && dependenciesPath.length > 0) {
            jdtBasedSpoonCompiler.setSourceClasspath(dependenciesPath);
        }
        try {
            jdtBasedSpoonCompiler.build();
        } catch (Exception e) {
            if (!continueWhileBuildFailure) {
                throw e;
            }
            e.printStackTrace();
        }
    }

    private Factory getFactory(Integer compilianceLevel) {
        if (factory == null) {
            return createFactory(compilianceLevel);
        }
        return factory;
    }

    private Factory createFactory(Integer compilianceLevel) {
        StandardEnvironment env = new StandardEnvironment();
        env.setComplianceLevel(compilianceLevel);
        env.setVerbose(false);
        env.setDebug(false);
        env.setTabulationSize(5);
        env.useTabulations(true);
        Factory factory = new FactoryImpl(new DefaultCoreFactory(), env);
        return factory;
    }

    public void getFactoryWithLauncher(String[] args) {
        launcher.setArgs(args);
        launcher.run();
        factory = launcher.getFactory();
    }

    public List<CtMethod> getMethods(CtCodeElement element){
        final ProcessingManager processingManager = new QueueProcessingManager(factory);
        final List<CtMethod> methods = new ArrayList<CtMethod>();
        AbstractProcessor<CtMethod> processor = new AbstractProcessor<CtMethod>() {
            public void process(CtMethod ctMethod) {
                methods.add(ctMethod);
            }
        };
        processingManager.addProcessor(processor);
        processingManager.process(element);
        return methods;
    }

    public List<CtInvocation> getMethodInvocations(CtCodeElement element){
        final ProcessingManager processingManager = new QueueProcessingManager(factory);
        final List<CtInvocation> invocations = new ArrayList<CtInvocation>();
        AbstractProcessor<CtInvocation> processor = new AbstractProcessor<CtInvocation>() {
            public void process(CtInvocation ctInvocation) {
                invocations.add(ctInvocation);
            }
        };
        processingManager.addProcessor(processor);
        processingManager.process(element);
        return invocations;
    }
    public CtClass getClass(String qualifiledClassName){
        List<CtType<?>> types = factory.Class().getAll();//cttype 不仅有class还有enum之类的
        return factory.Class().get(qualifiledClassName);//直接根据完整类名获取
    }

    public CtAnonymousExecutable getStaticInit(CtClass ctClass){
        for (Object anonymousExecutable : ctClass.getAnonymousExecutables()) {
            CtAnonymousExecutable executable = (CtAnonymousExecutable)anonymousExecutable;
            if(executable.getModifiers().contains(ModifierKind.STATIC)){
                return executable;
            }
        }
        return null;
    }

    public CtMethod<?> getMainEntry(CtClass<?> ctClass){
        for (CtMethod<?> ctMethod : getMethods(ctClass)) {
            if(ctMethod.getSimpleName().equals("main")){
                return  ctMethod;
            }
        }
        return null;
    }

    public List<CtMethod<?>> getOtherFunc(CtClass<?> ctClass){
        List<CtMethod<?>> ctMethods = new ArrayList<>();
        for (CtMethod<?> ctMethod : getMethods(ctClass)) {
            if(!ctMethod.getSimpleName().equals("main")){
                ctMethods.add(ctMethod);
            }
        }
        return ctMethods;
    }

    public Map<String, Set<String>> getInvocationInMethod(CtMethod<?> ctMethod){
        Map<String, Set<String>> className2funcNames = new HashMap<>();
        for (CtInvocation<?> methodInvocation : getMethodInvocations(ctMethod.getBody())) {
            CtExpression<?> ctExpression = methodInvocation.getTarget();
            if(ctExpression != null){
                CtTypeReference<?> ctTypeReference = ctExpression.getType();
                if(ctTypeReference.toString().equals("void")){
                    if(!className2funcNames.containsKey(ctExpression.toString())){
                        className2funcNames.put(ctExpression.toString(),new HashSet<>());
                    }
                    className2funcNames.get(ctExpression.toString()).add(methodInvocation.getExecutable().getSimpleName());
                }else {
                    if(!className2funcNames.containsKey(ctTypeReference.toString())){
                        className2funcNames.put(ctTypeReference.toString(),new HashSet<>());
                    }
                    className2funcNames.get(ctTypeReference.toString()).add(methodInvocation.getExecutable().getSimpleName());
                }
            }
        }
        return className2funcNames;
    }

    public CtMethod getTargetMethod(String className,String functionName){
        CtClass ctClass = getClass(className);
        if(ctClass == null){
            return null;
        }
        for (CtMethod method : getMethods(ctClass)) {
            if (method.getSimpleName().equals(functionName)) {
                return method;
            }
        }
        return null;
    }
}
