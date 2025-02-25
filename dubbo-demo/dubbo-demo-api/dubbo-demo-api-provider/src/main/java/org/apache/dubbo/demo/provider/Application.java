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
package org.apache.dubbo.demo.provider;

import org.apache.dubbo.api.demo.DemoService;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;

public class Application {

    private static final String REGISTRY_URL = "zookeeper://127.0.0.1:2181";

    public static void main(String[] args) {
        startWithBootstrap();
    }

    private static void startWithBootstrap() {
        /**
         * ServiceConfig是针对dubbo服务的一些配置信息
         * 泛型：DemoServiceImpl
         * 服务端提供的服务必须要有对应的实现代码，DemoServiceImpl就是服务接口的实现代码
         * （即某个对外暴露，提供服务的接口的实现类，DemoService接口即对外暴露的接口）
         */
        ServiceConfig<DemoServiceImpl> service = new ServiceConfig<>();
        service.setInterface(DemoService.class);    // 暴露出去的接口
        service.setRef(new DemoServiceImpl());      // 明确设置暴露接口的实现类

        // dubbo启动入口DubboBootstrap，表明provider是一个服务实例
        DubboBootstrap bootstrap = DubboBootstrap.getInstance();
        bootstrap
                .application(new ApplicationConfig("dubbo-demo-api-provider"))      // 服务名称
                // 所有rpc框架必须配合注册中心使用，服务一启动就去到注册中心注册
                // consumer必须通过注册中心询问，查看调用方的实例在那个机器上
                // 因为rpc框架中，consumer端调用远程方法的形式跟调用本地方法一样
                // consumer并不会管被调用方的地址，可以在rpc框架中用HashMap模拟一个注册中心
                // 将每个服务和对应的ip地址关联起来就行，这个HashMap的定义应该类似：HashMap<String, List<URL>>
                // 因为可能有服务器集群，所以是一个List<URL>
                .registry(new RegistryConfig(REGISTRY_URL))                               // 注册中心
                .protocol(new ProtocolConfig(CommonConstants.DUBBO, -1))
                .service(service)
                .start()    // 应该会启动一个网络监听的服务器
                .await();
    }
}
