package org.davidmoten.rx2.io;

import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.BiConsumer;

public final class Client {

    public static Flowable<ByteBuffer> read(URL url, int preRequest, int bufferSize) {
        BiConsumer<Long, Long> requester = new BiConsumer<Long, Long>() {

            @Override
            public void accept(Long id, Long request) throws Exception {
                // TODO Auto-generated method stub

            }

        };
        return Flowable.using( //
                () -> url.openConnection(), //
                con -> read(Single.fromCallable(() -> con.getInputStream()), requester, preRequest,
                        bufferSize), //
                con -> {
                    Util.close(con.getInputStream());
                    Util.close(con.getOutputStream());
                });
    }

    public static Flowable<ByteBuffer> read(Single<InputStream> inSource,
            BiConsumer<Long, Long> requester, int preRequest, int bufferSize) {
        return inSource
                .flatMapPublisher(in -> new FlowableHttp(in, requester, preRequest, bufferSize));
    }

}