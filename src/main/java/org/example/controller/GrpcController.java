package org.example.controller;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.grpc.proto.FileChunk;
import org.example.grpc.proto.GrpcServiceGrpc;
import org.example.grpc.proto.ResponseGRPC;
import org.example.service.FileReceiverService;
import org.example.utils.Constants;
import org.example.utils.filename.FileManager;

import java.nio.file.Files;
import java.nio.file.Path;


@RequiredArgsConstructor
@GrpcService
@Slf4j
public class GrpcController extends GrpcServiceGrpc.GrpcServiceImplBase {
    private final FileManager fileManager;
    private final FileReceiverService service;

    @Override
    public StreamObserver<FileChunk> upload(StreamObserver<ResponseGRPC> responseObs) {
        return new StreamObserver<>() {
            private String currFilename;
            private Path tempPath;

            @Override
            public void onNext(FileChunk chunk) {
                try {
                    if (currFilename == null) {
                        currFilename = chunk.getFilename();
                        log.info("start grpc receiving for file {}", currFilename);
                    }
                    tempPath = fileManager.appendChunk(currFilename, chunk.getData().toByteArray());
                    log.debug("append chunk in grpc for {}, offset: {}", currFilename, chunk.getOffset());
                } catch (Exception e) {
                    log.error("Failed to write chunk {}", currFilename, e);
                    responseObs.onError(e);
                }
            }

            @Override
            public void onCompleted() {
                try {
                    log.info("Upload completed for file: {}", currFilename);
                    long fileSize = Files.size(tempPath);
                    service.processFile(tempPath);

                    responseObs.onNext(ResponseGRPC.newBuilder()
                            .setStatus(Constants.STATUS_SUCCESS)
                            .setFilename(currFilename)
                            .setTotalBytes(fileSize)
                            .setMessage(Constants.MSG_UPLOAD_SUCCESS)
                            .build());
                    responseObs.onCompleted();
                } catch (Exception e) {
                    log.error("Failed to finalize upload {}", currFilename, e);
                    responseObs.onError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("gRPC upload failed", t);
                if (tempPath != null) {
                    try {
                        Files.deleteIfExists(tempPath);
                        log.info("Cleaned up temporary file: {}", tempPath);
                    } catch (Exception ignored) {
                        log.warn("Could not delete temporary file: {}", tempPath);
                    }
                }
            }
        };

    }
}

