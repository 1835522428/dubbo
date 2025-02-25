/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.demo.consumer;

import org.apache.dubbo.api.demo.DemoService;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.rpc.service.GenericService;

public class Application {

    private static final String REGISTRY_URL = "zookeeper://127.0.0.1:2181";

    public static void main(String[] args) {
        runWithBootstrap();
    }

    private static void runWithBootstrap() {
        /**
         * 服务端通过ServiceConfig将自己的接口实现暴露出去
         * 客户端通过ReferenceConfig提出自己要调用哪个接口的实现
         * ReferenceConfig本身是属于要调用的其他服务实例的引用的配置
         * 通过泛型传递要调用哪个接口
         */
        ReferenceConfig<DemoService> reference = new ReferenceConfig<>();
        reference.setInterface(DemoService.class);
        reference.setGeneric("true");

        // consumer本身也是一个服务实例
        DubboBootstrap bootstrap = DubboBootstrap.getInstance();
        bootstrap
                .application(new ApplicationConfig("dubbo-demo-api-consumer"))  // 要调用的服务名
                .registry(new RegistryConfig(REGISTRY_URL))                           // 注册中心地址
                .protocol(new ProtocolConfig(CommonConstants.TRIPLE, -1))
                .reference(reference)
                .start();

        // 这里就通过注册中心拿到了一个DemoService接口的实现类的实例，但是这个demoService其实是一个动态代理对象（JDK动态代理）
        DemoService demoService = bootstrap.getCache().get(reference);
        // 调用动态代理的方法，这个生成动态代理的动作应该是在Consumer端完成的？
        // 在Consumer端先给自己返回一个动态代理对象，等真正调用方法的时候调用h.invoke()
        // 这个invoke方法中应该是向远程服务端发起一个网络请求
        String message = demoService.sayHello("dubbo");
        System.out.println(message);

        // generic invoke
        GenericService genericService = (GenericService) demoService;
        Object genericInvokeResult = genericService.$invoke(
                "sayHello", new String[] {String.class.getName()}, new Object[] {"dubbo generic invoke"});
        System.out.println(genericInvokeResult.toString());
    }
}
