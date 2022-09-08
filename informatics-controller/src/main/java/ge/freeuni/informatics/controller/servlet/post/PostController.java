package ge.freeuni.informatics.controller.servlet.post;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.dto.PostDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.controller.model.*;
import ge.freeuni.informatics.server.posts.IPostsManager;
import ge.freeuni.informatics.server.posts.PostsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@RestController
public class PostController {

    @Autowired
    IPostsManager postsManager;
    @GetMapping(value = "/posts/{postId}/image", produces = MediaType.IMAGE_PNG_VALUE)
    byte[] getPostImage(@PathVariable Long postId) {
        try {
            File image = postsManager.getPostImage(postId);
            return Files.readAllBytes(image.toPath());
        } catch (InformaticsServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(value = "/{roomId}/posts")
    GetPostsResponse getPosts(@PathVariable Long roomId, PagingRequest request) {
        try {
            return new GetPostsResponse("SUCCESS", null, postsManager.getPosts(roomId, request.getOffset(), request.getLimit()));
        } catch (InformaticsServerException ex) {
            return new GetPostsResponse("FAIL", ex.getCode(), null);
        }
    }

    @GetMapping(value = "/posts/{postId}", produces = MediaType.IMAGE_PNG_VALUE)
    GetPostResponse getPost(@PathVariable Long postId) {
        try {
            return new GetPostResponse("SUCCESS", null, postsManager.getPost(postId));
        } catch (InformaticsServerException ex) {
            return new GetPostResponse("FAIL", ex.getCode(), null);
        }
    }


    @PostMapping(value = "/add-post", produces = MediaType.IMAGE_PNG_VALUE)
    InformaticsResponse addPost(@RequestBody PostDTO postDTO) {
        try {
            postsManager.addPost(postDTO);
            return new InformaticsResponse("SUCCESS", null);
        } catch (InformaticsServerException ex) {
            return new GetPostResponse("FAIL", ex.getCode(), null);
        }
    }

    @PostMapping(value = "/posts/upload", produces = MediaType.IMAGE_PNG_VALUE)
    InformaticsResponse uploadImage(@RequestParam MultipartFile image, @RequestParam Integer postId) {
        try {
            postsManager.uploadImage(postId, image.getBytes());
            return new InformaticsResponse("SUCCESS", null);
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse("FAIL", ex.getCode());
        } catch (IOException e) {
            return new InformaticsResponse("FAIL", "cantUploadFile");
        }
    }

}
