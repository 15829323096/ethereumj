/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.config.net;

import org.ethereum.config.blockchain.*;

import java.util.*;

/**
 * Convert JSON config from genesis to Java blockchain net config.
 * Created by Stan Reshetnyk on 23.12.2016.
 */
public class JsonNetConfig extends BaseNetConfig {

    public JsonNetConfig(Map<String, String> config) throws RuntimeException {
        final List<String> keys = Arrays.asList("homesteadBlock", "daoForkBlock", "EIP150Block", "EIP155Block", "EIP158Block");
        final ArrayList<Integer> orderedBlocks = new ArrayList<>();
        final HashMap<Integer, String> blockToKey = new HashMap<>();

        // #1 create block number to config map
        for (String key : keys) {
            Integer blockNumber = getInteger(config, key);
            if (blockNumber != null) {
                if (orderedBlocks.contains(blockNumber)) {
                    throw new RuntimeException("Genesis net config contains duplicate blocks");
                }
                orderedBlocks.add(blockNumber);
                blockToKey.put(blockNumber, key);
            }
        }

        // #2 sort block numbers before adding configs
        Collections.sort(orderedBlocks);

        // hardcoded initial config
        Integer prevBlockNumber = 0;    // frontier block
        add(0, new FrontierConfig());

        // #3 fill configs in proper order
        for (Integer blockNumber : orderedBlocks) {
            String key = blockToKey.get(blockNumber);
            switch (key) {
                case "homesteadBlock":
                    add(blockNumber, new HomesteadConfig());
                    break;
                case "daoForkBlock":
                    if ("true".equalsIgnoreCase(config.get("daoForkSupport"))) {
                        add(blockNumber, new DaoHFConfig().withForkBlock(blockNumber));
                    } else {
                        add(blockNumber, new DaoNoHFConfig().withForkBlock(blockNumber));
                    }
                    break;
                case "EIP150Block":
                    add(blockNumber, new Eip150HFConfig(getConfigForBlock(prevBlockNumber)));
                    break;
                // TODO handle other EIP configs
                default:
                    continue;
            }
            prevBlockNumber = blockNumber;
        }
    }

    private static Integer getInteger(Map<String, String> config, String key) {
        return config.containsKey(key) ? Integer.parseInt(config.get(key)) : null;
    }

}
