package com.example.backend;


import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestJpaAuditingConfig.class)
@SpringBootTest
@AutoConfigureMockMvc
class BackendApplicationTests {


}
