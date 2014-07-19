/*
 * Copyright (C) 2014 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dagger.internal.codegen;

import com.google.common.collect.ImmutableList;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

@RunWith(JUnit4.class)
public class ComponentProcessorTest {
  @Test public void componentOnConcreteClass() {
    JavaFileObject componentFile = JavaFileObjects.forSourceLines("test.NotAComponent",
        "package test;",
        "",
        "import dagger.Component;",
        "",
        "@Component",
        "final class NotAComponent {}");
    assert_().about(javaSource()).that(componentFile)
        .processedWith(new ComponentProcessor())
        .failsToCompile()
        .withErrorContaining("interface");
  }

  @Test public void componentOnEnum() {
    JavaFileObject componentFile = JavaFileObjects.forSourceLines("test.NotAComponent",
        "package test;",
        "",
        "import dagger.Component;",
        "",
        "@Component",
        "enum NotAComponent {",
        "  INSTANCE",
        "}");
    assert_().about(javaSource()).that(componentFile)
        .processedWith(new ComponentProcessor())
        .failsToCompile()
        .withErrorContaining("interface");
  }

  @Test public void componentOnAnnotation() {
    JavaFileObject componentFile = JavaFileObjects.forSourceLines("test.NotAComponent",
        "package test;",
        "",
        "import dagger.Component;",
        "",
        "@Component",
        "@interface NotAComponent {}");
    assert_().about(javaSource()).that(componentFile)
        .processedWith(new ComponentProcessor())
        .failsToCompile()
        .withErrorContaining("interface");
  }

  @Test public void nonModuleModule() {
    JavaFileObject componentFile = JavaFileObjects.forSourceLines("test.NotAComponent",
        "package test;",
        "",
        "import dagger.Component;",
        "",
        "@Component(modules = Object.class)",
        "interface NotAComponent {}");
    assert_().about(javaSource()).that(componentFile)
        .processedWith(new ComponentProcessor())
        .failsToCompile()
        .withErrorContaining("module");
  }

  @Test public void simpleComponent() {
    JavaFileObject injectableTypeFile = JavaFileObjects.forSourceLines("test.SomeInjectableType",
        "package test;",
        "",
        "import javax.inject.Inject;",
        "",
        "final class SomeInjectableType {",
        "  @Inject SomeInjectableType() {}",
        "}");
    JavaFileObject componentFile = JavaFileObjects.forSourceLines("test.SimpleComponent",
        "package test;",
        "",
        "import dagger.Component;",
        "import dagger.Lazy;",
        "",
        "import javax.inject.Provider;",
        "",
        "@Component",
        "interface SimpleComponent {",
        "  SomeInjectableType someInjectableType();",
        "  Lazy<SomeInjectableType> lazySomeInjectableType();",
        "  Provider<SomeInjectableType> someInjectableTypeProvider();",
        "}");
    JavaFileObject generatedComponent = JavaFileObjects.forSourceLines(
        "test.Dagger_SimpleComponent",
        "package test;",
        "",
        "import dagger.Lazy;",
        "import dagger.internal.DoubleCheckLazy;",
        "import javax.annotation.Generated;",
        "import javax.inject.Provider;",
        "",
        "@Generated(\"dagger.internal.codegen.ComponentProcessor\")",
        "public final class Dagger_SimpleComponent implements SimpleComponent {",
        "  private final Provider<SomeInjectableType> someInjectableTypeProvider;",
        "",
        "  private Dagger_SimpleComponent(Builder builder) {",
        "    assert builder != null;",
        "    this.someInjectableTypeProvider = new SomeInjectableType$$Factory();",
        "  }",
        "",
        "  public static Builder builder() {",
        "    return new Builder();",
        "  }",
        "",
        "  public static SimpleComponent create() {",
        "    return builder().build();",
        "  }",
        "",
        "  @Override",
        "  public SomeInjectableType someInjectableType() {",
        "    return someInjectableTypeProvider.get();",
        "  }",
        "",
        "  @Override",
        "  public Lazy<SomeInjectableType> lazySomeInjectableType() {",
        "    return DoubleCheckLazy.create(someInjectableTypeProvider);",
        "  }",
        "",
        "  @Override",
        "  public Provider<SomeInjectableType> someInjectableTypeProvider() {",
        "    return someInjectableTypeProvider;",
        "  }",
        "",
        "  public static final class Builder {",
        "    private Builder() {",
        "    }",
        "",
        "    public SimpleComponent build() {",
        "      return new Dagger_SimpleComponent(this);",
        "    }",
        "  }",
        "}");
    assert_().about(javaSources()).that(ImmutableList.of(injectableTypeFile, componentFile))
        .processedWith(new ComponentProcessor())
        .compilesWithoutError()
        .and().generatesSources(generatedComponent);
  }

  @Test public void componentWithScope() {
    JavaFileObject injectableTypeFile = JavaFileObjects.forSourceLines("test.SomeInjectableType",
        "package test;",
        "",
        "import javax.inject.Inject;",
        "import javax.inject.Singleton;",
        "",
        "@Singleton",
        "final class SomeInjectableType {",
        "  @Inject SomeInjectableType() {}",
        "}");
    JavaFileObject componentFile = JavaFileObjects.forSourceLines("test.SimpleComponent",
        "package test;",
        "",
        "import dagger.Component;",
        "import dagger.Lazy;",
        "import javax.inject.Provider;",
        "import javax.inject.Singleton;",
        "",
        "@Singleton",
        "@Component",
        "interface SimpleComponent {",
        "  SomeInjectableType someInjectableType();",
        "  Lazy<SomeInjectableType> lazySomeInjectableType();",
        "  Provider<SomeInjectableType> someInjectableTypeProvider();",
        "}");
    JavaFileObject generatedComponent = JavaFileObjects.forSourceLines(
        "test.Dagger_SimpleComponent",
        "package test;",
        "",
        "import dagger.Lazy;",
        "import dagger.internal.DoubleCheckLazy;",
        "import dagger.internal.ScopedProvider;",
        "import javax.annotation.Generated;",
        "import javax.inject.Provider;",
        "",
        "@Generated(\"dagger.internal.codegen.ComponentProcessor\")",
        "public final class Dagger_SimpleComponent implements SimpleComponent {",
        "  private final Provider<SomeInjectableType> someInjectableTypeProvider;",
        "",
        "  private Dagger_SimpleComponent(Builder builder) {",
        "    assert builder != null;",
        "    this.someInjectableTypeProvider =",
        "        ScopedProvider.create(new SomeInjectableType$$Factory());",
        "  }",
        "",
        "  public static Builder builder() {",
        "    return new Builder();",
        "  }",
        "",
        "  public static SimpleComponent create() {",
        "    return builder().build();",
        "  }",
        "",
        "  @Override",
        "  public SomeInjectableType someInjectableType() {",
        "    return someInjectableTypeProvider.get();",
        "  }",
        "",
        "  @Override",
        "  public Lazy<SomeInjectableType> lazySomeInjectableType() {",
        "    return DoubleCheckLazy.create(someInjectableTypeProvider);",
        "  }",
        "",
        "  @Override",
        "  public Provider<SomeInjectableType> someInjectableTypeProvider() {",
        "    return someInjectableTypeProvider;",
        "  }",
        "",
        "  public static final class Builder {",
        "    private Builder() {",
        "    }",
        "",
        "    public SimpleComponent build() {",
        "      return new Dagger_SimpleComponent(this);",
        "    }",
        "  }",
        "}");
    assert_().about(javaSources()).that(ImmutableList.of(injectableTypeFile, componentFile))
        .processedWith(new ComponentProcessor())
        .compilesWithoutError()
        .and().generatesSources(generatedComponent);
  }

  @Test public void simpleComponentWithNesting() {
    JavaFileObject nestedTypesFile = JavaFileObjects.forSourceLines("test.OuterType",
        "package test;",
        "",
        "import dagger.Component;",
        "import javax.inject.Inject;",
        "",
        "final class OuterType {",
        "  static class A {",
        "    @Inject A() {}",
        "  }",
        "  static class B {",
        "    @Inject A a;",
        "  }",
        "  @Component interface SimpleComponent {",
        "    A a();",
        "    void inject(B b);",
        "  }",
        "}");
    JavaFileObject aFactory = JavaFileObjects.forSourceLines(
        "test.OuterType$A$$Factory",
        "package test;",
        "",
        "import dagger.Factory;",
        "import javax.annotation.Generated;",
        "import test.OuterType.A;",
        "@Generated(\"dagger.internal.codegen.ComponentProcessor\")",
        "public final class OuterType$A$$Factory implements Factory<A> {",
        "",
        "  @Override public A get() {",
        "    return new A();",
        "  }",
        "}");
    JavaFileObject bMembersInjector = JavaFileObjects.forSourceLines(
        "test.OuterType$B$$MembersInjector",
        "package test;",
        "",
        "import dagger.MembersInjector;",
        "import javax.annotation.Generated;",
        "import javax.inject.Provider;",
        "import test.OuterType.A;",
        "import test.OuterType.B;",
        "",
        "@Generated(\"dagger.internal.codegen.ComponentProcessor\")",
        "public final class OuterType$B$$MembersInjector implements MembersInjector<B> {",
        "  private final Provider<A> aProvider;",
        "",
        "  public OuterType$B$$MembersInjector(Provider<A> aProvider) {",
        "    assert aProvider != null;",
        "    this.aProvider = aProvider;",
        "  }",
         "",
        "  @Override",
        "  public void injectMembers(B instance) {",
        "    if (instance == null) {",
        "      throw new NullPointerException(\"Cannot inject members into a null reference\");",
        "    }",
        "    instance.a = aProvider.get();",
        "  }",
        "}");

    JavaFileObject generatedComponent = JavaFileObjects.forSourceLines(
        "test.Dagger_OuterType$SimpleComponent",
        "package test;",
        "",
        "import dagger.MembersInjector;",
        "import javax.annotation.Generated;",
        "import javax.inject.Provider;",
        "import test.OuterType.A;",
        "import test.OuterType.B;",
        "import test.OuterType.SimpleComponent;",
        "",
        "@Generated(\"dagger.internal.codegen.ComponentProcessor\")",
        "public final class Dagger_OuterType$SimpleComponent implements SimpleComponent {",
        "  private final Provider<A> aProvider;",
        "  private final MembersInjector<B> bMembersInjector;",
        "",
        "  private Dagger_OuterType$SimpleComponent(Builder builder) {",
        "    assert builder != null;",
        "    this.aProvider = new OuterType$A$$Factory();",
        "    this.bMembersInjector = new OuterType$B$$MembersInjector(aProvider);",
        "  }",
        "",
        "  public static Builder builder() {",
        "    return new Builder();",
        "  }",
        "",
        "  public static SimpleComponent create() {",
        "    return builder().build();",
        "  }",
        "",
        "  @Override",
        "  public A a() {",
        "    return aProvider.get();",
        "  }",
        "",
        "  @Override",
        "  public void inject(B b) {",
        "    bMembersInjector.injectMembers(b);",
        "  }",
        "",
        "  public static final class Builder {",
        "    private Builder() {",
        "    }",
        "",
        "    public SimpleComponent build() {",
        "      return new Dagger_OuterType$SimpleComponent(this);",
        "    }",
        "  }",
        "}");
    assert_().about(javaSources()).that(ImmutableList.of(nestedTypesFile))
        .processedWith(new ComponentProcessor())
        .compilesWithoutError()
        .and().generatesSources(aFactory, bMembersInjector, generatedComponent);
  }

  @Test public void componentWithModule() {
    JavaFileObject aFile = JavaFileObjects.forSourceLines("test.A",
        "package test;",
        "",
        "import javax.inject.Inject;",
        "",
        "final class A {",
        "  @Inject A(B b) {}",
        "}");
    JavaFileObject bFile = JavaFileObjects.forSourceLines("test.B",
        "package test;",
        "",
        "interface B {}");
    JavaFileObject cFile = JavaFileObjects.forSourceLines("test.C",
        "package test;",
        "",
        "import javax.inject.Inject;",
        "",
        "final class C {",
        "  @Inject C() {}",
        "}");

    JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.TestModule",
        "package test;",
        "",
        "import dagger.Module;",
        "import dagger.Provides;",
        "",
        "@Module",
        "final class TestModule {",
        "  @Provides B b(C c) { return null; }",
        "}");

    JavaFileObject componentFile = JavaFileObjects.forSourceLines("test.TestComponent",
        "package test;",
        "",
        "import dagger.Component;",
        "",
        "import javax.inject.Provider;",
        "",
        "@Component(modules = TestModule.class)",
        "interface TestComponent {",
        "  A a();",
        "}");
    JavaFileObject generatedComponent = JavaFileObjects.forSourceLines(
        "test.Dagger_TestComponent",
        "package test;",
        "",
        "import javax.annotation.Generated;",
        "import javax.inject.Provider;",
        "",
        "@Generated(\"dagger.internal.codegen.ComponentProcessor\")",
        "public final class Dagger_TestComponent implements TestComponent {",
        "  private final TestModule testModule;",
        "  private final Provider<A> aProvider;",
        "  private final Provider<B> bProvider;",
        "  private final Provider<C> cProvider;",
        "",
        "  private Dagger_TestComponent(Builder builder) {",
        "    assert builder != null;",
        "    this.testModule = builder.testModule;",
        "    this.cProvider = new C$$Factory();",
        "    this.bProvider = new TestModule$$BFactory(testModule, cProvider);",
        "    this.aProvider = new A$$Factory(bProvider);",
        "  }",
        "",
        "  public static Builder builder() {",
        "    return new Builder();",
        "  }",
        "",
        "  public static TestComponent create() {",
        "    return builder().build();",
        "  }",
        "",
        "  @Override public A a() {",
        "    return aProvider.get();",
        "  }",
        "",
        "  public static final class Builder {",
        "    private TestModule testModule;",
        "",
        "    private Builder() {}",
        "",
        "    public TestComponent build() {",
        "      if (testModule == null) {",
        "        this.testModule = new TestModule();",
        "      }",
        "      return new Dagger_TestComponent(this);",
        "    }",
        "",
        "    public Builder testModule(TestModule testModule) {",
        "      if (testModule == null) {",
        "        throw new NullPointerException(\"testModule\");",
        "      }",
        "      this.testModule = testModule;",
        "      return this;",
        "    }",
        "  }",
        "}");
    assert_().about(javaSources())
        .that(ImmutableList.of(aFile, bFile, cFile, moduleFile, componentFile))
        .processedWith(new ComponentProcessor())
        .compilesWithoutError()
        .and().generatesSources(generatedComponent);
  }

  @Test public void setBindings() {
    JavaFileObject emptySetModuleFile = JavaFileObjects.forSourceLines("test.EmptySetModule",
        "package test;",
        "",
        "import static dagger.Provides.Type.SET_VALUES;",
        "",
        "import dagger.Module;",
        "import dagger.Provides;",
        "import java.util.Collections;",
        "import java.util.Set;",
        "",
        "@Module",
        "final class EmptySetModule {",
        "  @Provides(type = SET_VALUES) Set<String> emptySet() { return Collections.emptySet(); }",
        "}");
    JavaFileObject setModuleFile = JavaFileObjects.forSourceLines("test.SetModule",
        "package test;",
        "",
        "import static dagger.Provides.Type.SET;",
        "",
        "import dagger.Module;",
        "import dagger.Provides;",
        "",
        "@Module",
        "final class SetModule {",
        "  @Provides(type = SET) String string() { return \"\"; }",
        "}");
    JavaFileObject componentFile = JavaFileObjects.forSourceLines("test.TestComponent",
        "package test;",
        "",
        "import dagger.Component;",
        "import java.util.Set;",
        "",
        "import javax.inject.Provider;",
        "",
        "@Component(modules = {EmptySetModule.class, SetModule.class})",
        "interface TestComponent {",
        "  Set<String> strings();",
        "}");
    JavaFileObject generatedComponent = JavaFileObjects.forSourceLines(
        "test.Dagger_TestComponent",
        "package test;",
        "",
        "import dagger.internal.SetFactory;",
        "import java.util.Set;",
        "import javax.annotation.Generated;",
        "import javax.inject.Provider;",
        "",
        "@Generated(\"dagger.internal.codegen.ComponentProcessor\")",
        "public final class Dagger_TestComponent implements TestComponent {",
        "  private final EmptySetModule emptySetModule;",
        "  private final SetModule setModule;",
        "  private final Provider<Set<String>> setOfStringProvider;",
        "",
        "  private Dagger_TestComponent(Builder builder) {",
        "    assert builder != null;",
        "    this.emptySetModule = builder.emptySetModule;",
        "    this.setModule = builder.setModule;",
        "    this.setOfStringProvider = SetFactory.create(",
        "    new EmptySetModule$$EmptySetFactory(emptySetModule), new SetModule$$StringFactory(setModule));",
        "  }",
        "",
        "  public static Builder builder() {",
        "    return new Builder();",
        "  }",
        "",
        "  public static TestComponent create() {",
        "    return builder().build();",
        "  }",
        "",
        "  @Override",
        "  public Set<String> strings() {",
        "    return setOfStringProvider.get();",
        "  }",
        "",
        "  public static final class Builder {",
        "    private EmptySetModule emptySetModule;",
        "    private SetModule setModule;",
        "",
        "    private Builder() {",
        "    }",
        "",
        "    public TestComponent build() {",
        "      if (emptySetModule == null) {",
        "        this.emptySetModule = new EmptySetModule();",
        "      }",
        "      if (setModule == null) {",
        "        this.setModule = new SetModule();",
        "      }",
        "      return new Dagger_TestComponent(this);",
        "    }",
        "",
        "    public Builder emptySetModule(EmptySetModule emptySetModule) {",
        "      if (emptySetModule == null) {",
        "        throw new NullPointerException(\"emptySetModule\");",
        "      }",
        "      this.emptySetModule = emptySetModule;",
        "      return this;",
        "    }",
        "",
        "    public Builder setModule(SetModule setModule) {",
        "      if (setModule == null) {",
        "        throw new NullPointerException(\"setModule\");",
        "      }",
        "      this.setModule = setModule;",
        "      return this;",
        "    }",
        "  }",
        "}");
    assert_().about(javaSources())
        .that(ImmutableList.of(emptySetModuleFile, setModuleFile, componentFile))
        .processedWith(new ComponentProcessor())
        .compilesWithoutError()
        .and().generatesSources(generatedComponent);
  }

  @Test public void membersInjection() {
    JavaFileObject injectableTypeFile = JavaFileObjects.forSourceLines("test.SomeInjectableType",
        "package test;",
        "",
        "import javax.inject.Inject;",
        "",
        "final class SomeInjectableType {",
        "  @Inject SomeInjectableType() {}",
        "}");
    JavaFileObject injectedTypeFile = JavaFileObjects.forSourceLines("test.SomeInjectedType",
        "package test;",
        "",
        "import javax.inject.Inject;",
        "",
        "final class SomeInjectedType {",
        "  @Inject SomeInjectableType injectedField;",
        "  SomeInjectedType() {}",
        "}");
    JavaFileObject componentFile = JavaFileObjects.forSourceLines("test.SimpleComponent",
        "package test;",
        "",
        "import dagger.Component;",
        "import dagger.Lazy;",
        "",
        "import javax.inject.Provider;",
        "",
        "@Component",
        "interface SimpleComponent {",
        "  void inject(SomeInjectedType instance);",
        "  SomeInjectedType injectAndReturn(SomeInjectedType instance);",
        "}");
    JavaFileObject generatedComponent = JavaFileObjects.forSourceLines(
        "test.Dagger_SimpleComponent",
        "package test;",
        "",
        "import dagger.MembersInjector;",
        "import javax.annotation.Generated;",
        "import javax.inject.Provider;",
        "",
        "@Generated(\"dagger.internal.codegen.ComponentProcessor\")",
        "public final class Dagger_SimpleComponent implements SimpleComponent {",
        "  private final Provider<SomeInjectableType> someInjectableTypeProvider;",
        "  private final MembersInjector<SomeInjectedType> someInjectedTypeMembersInjector;",
        "",
        "  private Dagger_SimpleComponent(Builder builder) {",
        "    assert builder != null;",
        "    this.someInjectableTypeProvider = new SomeInjectableType$$Factory();",
        "    this.someInjectedTypeMembersInjector =",
        "        new SomeInjectedType$$MembersInjector(someInjectableTypeProvider);",
        "  }",
        "",
        "  public static Builder builder() {",
        "    return new Builder();",
        "  }",
        "",
        "  public static SimpleComponent create() {",
        "    return builder().build();",
        "  }",
        "",
        "  @Override public void inject(SomeInjectedType instance) {",
        "    someInjectedTypeMembersInjector.injectMembers(instance);",
        "  }",
        "",
        "  @Override public SomeInjectedType injectAndReturn(SomeInjectedType instance) {",
        "    someInjectedTypeMembersInjector.injectMembers(instance);",
        "    return instance;",
        "  }",
        "",
        "  public static final class Builder {",
        "    private Builder() {}",
        "",
        "    public SimpleComponent build() {",
        "      return new Dagger_SimpleComponent(this);",
        "    }",
        "  }",
        "}");
    assert_().about(javaSources())
        .that(ImmutableList.of(injectableTypeFile, injectedTypeFile, componentFile))
        .processedWith(new ComponentProcessor())
        .compilesWithoutError()
        .and().generatesSources(generatedComponent);
  }

  @Test public void componentInjection() {
    JavaFileObject injectableTypeFile = JavaFileObjects.forSourceLines("test.SomeInjectableType",
        "package test;",
        "",
        "import javax.inject.Inject;",
        "",
        "final class SomeInjectableType {",
        "  @Inject SomeInjectableType(SimpleComponent component) {}",
        "}");
    JavaFileObject componentFile = JavaFileObjects.forSourceLines("test.SimpleComponent",
        "package test;",
        "",
        "import dagger.Component;",
        "import dagger.Lazy;",
        "",
        "import javax.inject.Provider;",
        "",
        "@Component",
        "interface SimpleComponent {",
        "  SomeInjectableType someInjectableType();",
        "}");
    JavaFileObject generatedComponent = JavaFileObjects.forSourceLines(
        "test.Dagger_SimpleComponent",
        "package test;",
        "",
        "import dagger.internal.InstanceFactory;",
        "import javax.annotation.Generated;",
        "import javax.inject.Provider;",
        "",
        "@Generated(\"dagger.internal.codegen.ComponentProcessor\")",
        "public final class Dagger_SimpleComponent implements SimpleComponent {",
        "  private final Provider<SimpleComponent> simpleComponentProvider;",
        "  private final Provider<SomeInjectableType> someInjectableTypeProvider;",
        "",
        "  private Dagger_SimpleComponent(Builder builder) {",
        "    assert builder != null;",
        "    this.simpleComponentProvider = InstanceFactory.<SimpleComponent>create(this);",
        "    this.someInjectableTypeProvider =",
        "        new SomeInjectableType$$Factory(simpleComponentProvider);",
        "  }",
        "",
        "  public static Builder builder() {",
        "    return new Builder();",
        "  }",
        "",
        "  public static SimpleComponent create() {",
        "    return builder().build();",
        "  }",
        "",
        "  @Override",
        "  public SomeInjectableType someInjectableType() {",
        "    return someInjectableTypeProvider.get();",
        "  }",
        "",
        "  public static final class Builder {",
        "    private Builder() {",
        "    }",
        "",
        "    public SimpleComponent build() {",
        "      return new Dagger_SimpleComponent(this);",
        "    }",
        "  }",
        "}");
    assert_().about(javaSources()).that(ImmutableList.of(injectableTypeFile, componentFile))
        .processedWith(new ComponentProcessor())
        .compilesWithoutError()
        .and().generatesSources(generatedComponent);
  }

  @Test public void membersInjectionInsideProvision() {
    JavaFileObject injectableTypeFile = JavaFileObjects.forSourceLines("test.SomeInjectableType",
        "package test;",
        "",
        "import javax.inject.Inject;",
        "",
        "final class SomeInjectableType {",
        "  @Inject SomeInjectableType() {}",
        "}");
    JavaFileObject injectedTypeFile = JavaFileObjects.forSourceLines("test.SomeInjectedType",
        "package test;",
        "",
        "import javax.inject.Inject;",
        "",
        "final class SomeInjectedType {",
        "  @Inject SomeInjectableType injectedField;",
        "  @Inject SomeInjectedType() {}",
        "}");
    JavaFileObject componentFile = JavaFileObjects.forSourceLines("test.SimpleComponent",
        "package test;",
        "",
        "import dagger.Component;",
        "",
        "@Component",
        "interface SimpleComponent {",
        "  SomeInjectedType createAndInject();",
        "}");
    JavaFileObject generatedComponent = JavaFileObjects.forSourceLines(
        "test.Dagger_SimpleComponent",
        "package test;",
        "",
        "import dagger.MembersInjector;",
        "import javax.annotation.Generated;",
        "import javax.inject.Provider;",
        "",
        "@Generated(\"dagger.internal.codegen.ComponentProcessor\")",
        "public final class Dagger_SimpleComponent implements SimpleComponent {",
        "  private final Provider<SomeInjectableType> someInjectableTypeProvider;",
        "  private final Provider<SomeInjectedType> someInjectedTypeProvider;",
        "  private final MembersInjector<SomeInjectedType> someInjectedTypeMembersInjector;",
        "",
        "  private Dagger_SimpleComponent(Builder builder) {",
        "    assert builder != null;",
        "    this.someInjectableTypeProvider = new SomeInjectableType$$Factory();",
        "    this.someInjectedTypeMembersInjector =",
        "        new SomeInjectedType$$MembersInjector(someInjectableTypeProvider);",
        "    this.someInjectedTypeProvider =",
        "        new SomeInjectedType$$Factory(someInjectedTypeMembersInjector);",
        "  }",
        "",
        "  public static Builder builder() {",
        "    return new Builder();",
        "  }",
        "",
        "  public static SimpleComponent create() {",
        "    return builder().build();",
        "  }",
        "",
        "  @Override",
        "  public SomeInjectedType createAndInject() {",
        "    return someInjectedTypeProvider.get();",
        "  }",
        "",
        "  public static final class Builder {",
        "    private Builder() {",
        "    }",
        "",
        "    public SimpleComponent build() {",
        "      return new Dagger_SimpleComponent(this);",
        "    }",
        "  }",
        "}");
    assert_().about(javaSources())
        .that(ImmutableList.of(injectableTypeFile, injectedTypeFile, componentFile))
        .processedWith(new ComponentProcessor())
        .compilesWithoutError()
        .and().generatesSources(generatedComponent);
  }

  @Test public void componentDependency() {
    JavaFileObject aFile = JavaFileObjects.forSourceLines("test.A",
        "package test;",
        "",
        "import javax.inject.Inject;",
        "",
        "final class A {",
        "  @Inject A() {}",
        "}");
    JavaFileObject bFile = JavaFileObjects.forSourceLines("test.B",
        "package test;",
        "",
        "import javax.inject.Inject;",
        "",
        "final class B {",
        "  @Inject B(A a) {}",
        "}");
    JavaFileObject aComponentFile = JavaFileObjects.forSourceLines("test.AComponent",
        "package test;",
        "",
        "import dagger.Component;",
        "import dagger.Lazy;",
        "",
        "import javax.inject.Provider;",
        "",
        "@Component",
        "interface AComponent {",
        "  A a();",
        "}");
    JavaFileObject bComponentFile = JavaFileObjects.forSourceLines("test.AComponent",
        "package test;",
        "",
        "import dagger.Component;",
        "import dagger.Lazy;",
        "",
        "import javax.inject.Provider;",
        "",
        "@Component(dependencies = AComponent.class)",
        "interface BComponent {",
        "  B b();",
        "}");
    JavaFileObject generatedComponent = JavaFileObjects.forSourceLines(
        "test.Dagger_BComponent",
        "package test;",
        "",
        "import dagger.Factory;",
        "import javax.annotation.Generated;",
        "import javax.inject.Provider;",
        "",
        "@Generated(\"dagger.internal.codegen.ComponentProcessor\")",
        "public final class Dagger_BComponent implements BComponent {",
        "  private final AComponent aComponent;",
        "  private final Provider<A> aProvider;",
        "  private final Provider<B> bProvider;",
        "",
        "  private Dagger_BComponent(Builder builder) {  ",
        "    assert builder != null;",
        "    this.aComponent = builder.aComponent;",
        "    this.aProvider = new Factory<A>() {",
        "      @Override public A get() {",
        "        return aComponent.a();",
        "      }",
        "    };",
        "    this.bProvider = new B$$Factory(aProvider);",
        "  }",
        "",
        "  public static Builder builder() {  ",
        "    return new Builder();",
        "  }",
        "",
        "  @Override",
        "  public B b() {  ",
        "    return bProvider.get();",
        "  }",
        "",
        "  public static final class Builder {",
        "    private AComponent aComponent;",
        "  ",
        "    private Builder() {  ",
        "    }",
        "  ",
        "    public BComponent build() {  ",
        "      if (aComponent == null) {",
        "        throw new IllegalStateException(\"aComponent must be set\");",
        "      }",
        "      return new Dagger_BComponent(this);",
        "    }",
        "  ",
        "    public Builder aComponent(AComponent aComponent) {  ",
        "      if (aComponent == null) {",
        "        throw new NullPointerException(\"aComponent\");",
        "      }",
        "      this.aComponent = aComponent;",
        "      return this;",
        "    }",
        "  }",
        "}");
    assert_().about(javaSources())
        .that(ImmutableList.of(aFile, bFile, aComponentFile, bComponentFile))
        .processedWith(new ComponentProcessor())
        .compilesWithoutError()
        .and().generatesSources(generatedComponent);
  }

  @Test public void moduleNameCollision() {
    JavaFileObject aFile = JavaFileObjects.forSourceLines("test.A",
        "package test;",
        "",
        "public final class A {}");
    JavaFileObject otherAFile = JavaFileObjects.forSourceLines("other.test.A",
        "package other.test;",
        "",
        "public final class A {}");

    JavaFileObject moduleFile = JavaFileObjects.forSourceLines("test.TestModule",
        "package test;",
        "",
        "import dagger.Module;",
        "import dagger.Provides;",
        "",
        "@Module",
        "public final class TestModule {",
        "  @Provides A a() { return null; }",
        "}");
    JavaFileObject otherModuleFile = JavaFileObjects.forSourceLines("other.test.TestModule",
        "package other.test;",
        "",
        "import dagger.Module;",
        "import dagger.Provides;",
        "",
        "@Module",
        "public final class TestModule {",
        "  @Provides A a() { return null; }",
        "}");

    JavaFileObject componentFile = JavaFileObjects.forSourceLines("test.TestComponent",
        "package test;",
        "",
        "import dagger.Component;",
        "",
        "import javax.inject.Provider;",
        "",
        "@Component(modules = {TestModule.class, other.test.TestModule.class})",
        "interface TestComponent {",
        "  A a();",
        "  other.test.A otherA();",
        "}");
    JavaFileObject generatedComponent = JavaFileObjects.forSourceLines(
        "test.Dagger_TestComponent",
        "package test;",
        "",
        "import javax.annotation.Generated;",
        "import javax.inject.Provider;",
        "import other.test.A;",
        "import other.test.TestModule;",
        "import other.test.TestModule$$AFactory;",
        "",
        "@Generated(\"dagger.internal.codegen.ComponentProcessor\")",
        "public final class Dagger_TestComponent implements TestComponent {",
        "  private final test.TestModule testModule;",
        "  private final TestModule testModule1;",
        "  private final Provider<A> aProvider;",
        "  private final Provider<test.A> aProvider1;",
        "",
        "  private Dagger_TestComponent(Builder builder) {",
        "    assert builder != null;",
        "    this.testModule = builder.testModule;",
        "    this.testModule1 = builder.testModule1;",
        "    this.aProvider1 = new test.TestModule$$AFactory(testModule);",
        "    this.aProvider = new TestModule$$AFactory(testModule1);",
        "  }",
        "",
        "  public static Builder builder() {",
        "    return new Builder();",
        "  }",
        "",
        "  public static TestComponent create() {",
        "    return builder().build();",
        "  }",
        "",
        "  @Override public test.A a() {",
        "    return aProvider1.get();",
        "  }",
        "",
        "  @Override public A otherA() {",
        "    return aProvider.get();",
        "  }",
        "",
        "  public static final class Builder {",
        "    private test.TestModule testModule;",
        "    private TestModule testModule1;",
        "",
        "    private Builder() {}",
        "",
        "    public TestComponent build() {",
        "      if (testModule == null) {",
        "        this.testModule = new test.TestModule();",
        "      }",
        "      if (testModule1 == null) {",
        "        this.testModule1 = new TestModule();",
        "      }",
        "      return new Dagger_TestComponent(this);",
        "    }",
        "",
        "    public Builder testModule(test.TestModule testModule) {",
        "      if (testModule == null) {",
        "        throw new NullPointerException(\"testModule\");",
        "      }",
        "      this.testModule = testModule;",
        "      return this;",
        "    }",
        "",
        "    public Builder testModule(TestModule testModule) {",
        "      if (testModule == null) {",
        "        throw new NullPointerException(\"testModule\");",
        "      }",
        "      this.testModule1 = testModule;",
        "      return this;",
        "    }",
        "  }",
        "}");
    assert_().about(javaSources())
        .that(ImmutableList.of(aFile, otherAFile, moduleFile, otherModuleFile, componentFile))
        .processedWith(new ComponentProcessor())
        .compilesWithoutError()
        .and().generatesSources(generatedComponent);
  }
}
