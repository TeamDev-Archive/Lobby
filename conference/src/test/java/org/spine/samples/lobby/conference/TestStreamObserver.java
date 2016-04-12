/*
 * Copyright 2015, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.spine.samples.lobby.conference;

import com.google.common.base.Function;
import io.grpc.stub.StreamObserver;

/**
 * @author andrii.loboda
 */
public class TestStreamObserver<T> implements StreamObserver<T> {
    private final Function<T, Void> nextFunction;
    private final Function<Throwable, Void> onErrorFunction;
    private final Function<Void, Void> onCompleteFunction;

    TestStreamObserver(Function<T, Void> nextFunction, Function<Throwable, Void> onErrorFunction, Function<Void, Void> onCompleteFunction) {
        this.nextFunction = nextFunction;
        this.onErrorFunction = onErrorFunction;
        this.onCompleteFunction = onCompleteFunction;
    }

    @Override
    public void onNext(T t) {
        if (nextFunction != null) {
            nextFunction.apply(t);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (onErrorFunction != null) {
            onErrorFunction.apply(throwable);
        }
    }

    @Override
    public void onCompleted() {
        if (onCompleteFunction != null) {
            onCompleteFunction.apply(null);
        }
    }

    public static <T> Builder<T> newBuilder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private Function<T, Void> nextFunction;
        private Function<Throwable, Void> onErrorFunction;
        private Function<Void, Void> onCompleteFunction;

        public Builder setNextFunction(Function<T, Void> nextFunction) {
            this.nextFunction = nextFunction;
            return this;
        }

        public Builder setOnErrorFunction(Function<Throwable, Void> onErrorFunction) {
            this.onErrorFunction = onErrorFunction;
            return this;
        }

        public Builder setOnCompleteFunction(Function<Void, Void> onCompleteFunction) {
            this.onCompleteFunction = onCompleteFunction;
            return this;
        }

        public TestStreamObserver<T> build() {
            return new TestStreamObserver<>(nextFunction, onErrorFunction, onCompleteFunction);
        }


    }

}
