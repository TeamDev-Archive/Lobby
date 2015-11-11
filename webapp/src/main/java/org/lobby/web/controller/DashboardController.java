package org.lobby.web.controller;

import org.lobby.web.controller.response.SimpleResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Alexander Litus
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @RequestMapping("/test")
    @ResponseBody
    public SimpleResponse getTestData(){
        return SimpleResponse.create(3);
    }
}
