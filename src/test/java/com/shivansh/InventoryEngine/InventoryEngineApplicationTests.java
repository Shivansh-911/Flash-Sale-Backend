package com.shivansh.InventoryEngine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class InventoryEngineApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
    private StringRedisTemplate stringRedisTemplate;

    // @BeforeEach
	// void cleanRedis() {
    // stringRedisTemplate.getConnectionFactory()
    //              .getConnection()
    //              .serverCommands()
    //              .flushDb();
	// }

}
