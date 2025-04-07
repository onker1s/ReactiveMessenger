import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import server.ServerApplication;

@SpringBootTest(classes = ServerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)

class ServerApplicationTest {

    @Test
    void contextLoads() {
    }

}
