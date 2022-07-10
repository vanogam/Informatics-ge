package ge.freeuni.informatics.controller.servlet.contest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContestController {

    @GetMapping("/contest-list")
    public void getContestList(@RequestParam Integer roomId) {

    }
}
