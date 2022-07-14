package com.easterlyn.util.wrapper;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;

public record PlayerFuture(String id, CompletableFuture<Optional<Player>> future) {}
