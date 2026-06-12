package com.example.agent.dto;

import lombok.Data;
//不要直接拿 Entity 接收前端请求，这是好习惯。
@Data
public class UserDTO {
    private String username;
    private String password;
}
