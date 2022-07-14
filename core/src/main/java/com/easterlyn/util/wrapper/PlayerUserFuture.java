package com.easterlyn.util.wrapper;

import com.easterlyn.user.PlayerUser;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record PlayerUserFuture(String id, CompletableFuture<Optional<PlayerUser>> future) {}
