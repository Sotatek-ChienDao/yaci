package com.bloxbean.cardano.yaci.helper;

import com.bloxbean.cardano.yaci.core.model.Block;
import com.bloxbean.cardano.yaci.core.protocol.chainsync.messages.Point;
import com.bloxbean.cardano.yaci.helper.listener.BlockChainDataListener;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class BlockRangeSyncIT extends BaseTest{

    @Test
    void fetch() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        List<Block> blocks = new ArrayList<>();
        BlockRangeSync blockRangeSync = new BlockRangeSync(node, nodePort, protocolMagic);
        blockRangeSync.start(new BlockChainDataListener() {
            @Override
            public void onBlock(Block block) {
                System.out.println(block.getHeader().getHeaderBody().getBlockNumber());
                blocks.add(block);
                countDownLatch.countDown();
            }
        });

        Point from = new Point(13107194, "5b6194ab222088fdd0f3bad7e7343808ec10e52db107160e412bc204f58cf020");
        Point to = new Point(13107220, "b26af1198cb891107fafead2388c8e8019f0157d101fda7896ba7358b58c7b83");
        blockRangeSync.fetch(from, to);

        countDownLatch.await(60, TimeUnit.SECONDS);
        assertThat(blocks).hasSize(3);
        assertThat(blocks.get(0).getHeader().getHeaderBody().getSlot()).isEqualTo(13107194);
        assertThat(blocks.get(2).getHeader().getHeaderBody().getSlot()).isEqualTo(13107220);
    }
}
