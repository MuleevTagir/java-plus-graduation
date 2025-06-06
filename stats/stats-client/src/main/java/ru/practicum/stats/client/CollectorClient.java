package ru.practicum.stats.client;

import com.google.protobuf.Empty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CollectorClient {
    @GrpcClient("collector")
    UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public void sendUserAction(UserActionProto action) {
        Empty empty = client.collectUserAction(action);
    }
}
