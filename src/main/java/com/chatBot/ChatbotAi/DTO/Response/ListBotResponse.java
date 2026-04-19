package com.chatBot.ChatbotAi.DTO.Response;

import java.util.List;
import java.util.Optional;

public class ListBotResponse extends Response {
    public Boolean hasData;
    public Optional<List<ListBot>> listBot;

    public ListBotResponse(Boolean hasData) {
        this.hasData = hasData;
    }

    public ListBotResponse(List<ListBot> listBot) {
        this.hasData = true;
        this.listBot = Optional.ofNullable(listBot);
    }
}
