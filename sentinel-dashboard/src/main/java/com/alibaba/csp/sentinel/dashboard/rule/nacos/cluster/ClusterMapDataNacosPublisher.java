/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.rule.nacos.cluster;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.ClusterGroupEntity;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.request.ClusterAppAssignMap;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.nacos.api.config.ConfigService;

/**
 * 流控规则 NacosPublisher
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
@Component("clusterMapDataNacosPublisher")
public class ClusterMapDataNacosPublisher
    implements DynamicRulePublisher<List<ClusterAppAssignMap>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClusterMapDataNacosPublisher.class);
  @Autowired private ConfigService configService;
  @Autowired private Converter<List<ClusterGroupEntity>, String> converter;

  @Override
  public void publish(String app, List<ClusterAppAssignMap> clusterAppAssignMapList)
      throws Exception {
    AssertUtil.notEmpty(app, "app name cannot be empty");
    if (clusterAppAssignMapList == null) {
      return;
    }
    List<ClusterGroupEntity> clusterGroupEntityList = new ArrayList<>();
    for (ClusterAppAssignMap clusterAppAssignMap : clusterAppAssignMapList) {
      ClusterGroupEntity clusterGroupEntity = new ClusterGroupEntity();
      clusterGroupEntity.setMachineId(clusterAppAssignMap.getMachineId());
      clusterGroupEntity.setIp(clusterAppAssignMap.getIp());
      clusterGroupEntity.setPort(clusterAppAssignMap.getPort());
      clusterGroupEntity.setClientSet(clusterAppAssignMap.getClientSet());
      clusterGroupEntity.setBelongToApp(clusterAppAssignMap.getBelongToApp());

      clusterGroupEntityList.add(clusterGroupEntity);
    }
    String convertedRule = converter.convert(clusterGroupEntityList);
    LOGGER.info("sentinel dashboard publish cluster map: {}", convertedRule);
    configService.publishConfig(
        app + NacosConfigUtil.CLUSTER_MAP_DATA_ID_POSTFIX, NacosConfigUtil.GROUP_ID, convertedRule);
  }
}
