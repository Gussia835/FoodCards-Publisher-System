package org.example.controller;

import io.grpc.stub.StreamObserver;
import org.example.grpc.proto.FileChunk;
import org.example.grpc.proto.ResponseGRPC;
import org.example.service.FileReceiverService;
import org.example.utils.filename.FileManager;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrpcControllerTest {

    @Mock
    private FileManager fileManager;

    @Mock
    private FileReceiverService service;

    @InjectMocks
    private GrpcController grpcController;

    @Captor
    private ArgumentCaptor<Path> pathCaptor;

    @TempDir
    Path tempDir;

    private static final String VALID_FILENAME = "Z001032.GLAER_ENROLL0010321.249";
    private static final byte[] CHUNK_DATA_1 = "chunk1".getBytes();
    private static final byte[] CHUNK_DATA_2 = "chunk2".getBytes();

    @Nested
    class SuccessTests {

        @Test
        void validGrpcStreamTest() throws Exception {
            Path inProgressPath = tempDir.resolve(VALID_FILENAME);
            Files.createFile(inProgressPath);

            when(fileManager.appendChunk(eq(VALID_FILENAME), any(byte[].class))).thenReturn(inProgressPath);
            doNothing().when(service).processFile(any(Path.class));

            StreamObserver<ResponseGRPC> responseObserver = mock(StreamObserver.class);
            StreamObserver<FileChunk> requestObserver = grpcController.upload(responseObserver);

            requestObserver.onNext(FileChunk.newBuilder()
                    .setFilename(VALID_FILENAME)
                    .setData(com.google.protobuf.ByteString.copyFrom(CHUNK_DATA_1))
                    .build());

            requestObserver.onNext(FileChunk.newBuilder()
                    .setFilename(VALID_FILENAME)
                    .setData(com.google.protobuf.ByteString.copyFrom(CHUNK_DATA_2))
                    .build());

            requestObserver.onCompleted();

            verify(service, times(1)).processFile(pathCaptor.capture());
            assertThat(pathCaptor.getValue().getFileName().toString()).isEqualTo(VALID_FILENAME);
            verify(responseObserver).onNext(any(ResponseGRPC.class));
            verify(responseObserver).onCompleted();
        }
    }

    @Nested
    class ErrorTests {

        @Test
        void chunkWriteErrorTest() {
            when(fileManager.appendChunk(eq(VALID_FILENAME), any(byte[].class)))
                    .thenThrow(new RuntimeException("Disk full"));

            StreamObserver<ResponseGRPC> responseObserver = mock(StreamObserver.class);
            StreamObserver<FileChunk> requestObserver = grpcController.upload(responseObserver);

            requestObserver.onNext(FileChunk.newBuilder()
                    .setFilename(VALID_FILENAME)
                    .setData(com.google.protobuf.ByteString.copyFrom(CHUNK_DATA_1))
                    .build());
            verify(responseObserver).onError(any(Throwable.class));
            verify(service, never()).processFile(any());
        }

        @Test
        void processingErrorTest() throws Exception {
            Path inProgressPath = tempDir.resolve(VALID_FILENAME);
            Files.createFile(inProgressPath);

            when(fileManager.appendChunk(eq(VALID_FILENAME), any(byte[].class))).thenReturn(inProgressPath);
            doThrow(new RuntimeException("Processing failed")).when(service).processFile(any(Path.class));

            StreamObserver<ResponseGRPC> responseObserver = mock(StreamObserver.class);
            StreamObserver<FileChunk> requestObserver = grpcController.upload(responseObserver);

            requestObserver.onNext(FileChunk.newBuilder()
                    .setFilename(VALID_FILENAME)
                    .setData(com.google.protobuf.ByteString.copyFrom(CHUNK_DATA_1))
                    .build());
            requestObserver.onCompleted();
            verify(responseObserver).onError(any(Throwable.class));
        }
    }
}