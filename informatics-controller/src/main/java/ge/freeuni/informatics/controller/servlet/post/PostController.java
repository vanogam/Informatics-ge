package ge.freeuni.informatics.controller.servlet.post;

import ge.freeuni.informatics.common.dto.PostCommentDTO;
import ge.freeuni.informatics.common.dto.PostDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.controller.model.*;
import ge.freeuni.informatics.server.files.FileManager;
import ge.freeuni.informatics.server.posts.IPostsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PostController {

    @Autowired
    IPostsManager postsManager;

    @Autowired
    FileManager fileManager;

    @Value("${ge.freeuni.informatics.defaultPageSize}")
    Integer defaultPageSize;

    @Value("${ge.freeuni.informatics.maxImageSizeMB}")
    Integer maxImageSize;

    @GetMapping(value = "/room/{roomId}/posts")
    ResponseEntity<GetPostsResponse> getPosts(@PathVariable Long roomId, PagingRequest request) {
        try {
            if (request.getLimit() == null) {
                request.setLimit(defaultPageSize);
            } else if (request.getLimit() < 1) {
                request.setLimit(1);
            }
            if (request.getOffset() == null) {
                request.setOffset(0);
            } else if (request.getOffset() < 0) {
                request.setOffset(0);
            }
            return ResponseEntity.ok(new GetPostsResponse(postsManager.getPosts(roomId, request.getLimit(), request.getOffset())));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().body(new GetPostsResponse(ex.getCode(), null));
        }
    }

    @GetMapping(value = "/post/{postId}")
    ResponseEntity<PostDTO> getPost(@PathVariable Long postId) {
        try {
            return ResponseEntity.ok(postsManager.getPost(postId));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }


    @PostMapping(value = "/room/{roomId}/post")
    ResponseEntity<AddPostResponse> addPostDraft(@PathVariable Long roomId, @RequestBody PostDTO postDTO) throws InformaticsServerException {
        try {
            return ResponseEntity.ok(new AddPostResponse(null, postsManager.addPostDraft(roomId, postDTO)));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PutMapping(value = "/post/{postId}")
    ResponseEntity<Void> savePost(@PathVariable Long postId, @RequestBody PostDTO postDTO) {
        try {
            postsManager.savePost(postDTO);
            return ResponseEntity.ok().build();
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/post/{postId}/image")
    ResponseEntity<ImageUploadResponse> uploadImage(@RequestParam MultipartFile file, @PathVariable Long postId) {
        if (file.getSize() > maxImageSize * 1024 * 1024) {
            return ResponseEntity.badRequest().body(new ImageUploadResponse("maxSizeExceeded"));
        }
        try {
            return ResponseEntity.ok(new ImageUploadResponse(fileManager.saveFileForPost(postId, file.getBytes())));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().body(new ImageUploadResponse(true, ex.getCode()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(new ImageUploadResponse(true, "fileUploadError"));
        }
    }

    @GetMapping("/posts/{postId}/image/{filename}")
    public ResponseEntity<byte[]> downloadImage(@PathVariable Long postId, @PathVariable String filename) {
        try {
            PostDTO postDTO = postsManager.getPost(postId);
            byte[] fileContent = fileManager.getFileForPost(postDTO.roomId(), postId.intValue(), filename);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(fileContent);
        } catch (IOException | InformaticsServerException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(value = "/posts/{postId}")
    ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        try {
            postsManager.deletePost(postId);
            return ResponseEntity.ok().build();
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/posts/{postId}/comment")
    ResponseEntity<Void> addComment(@PathVariable Long postId, @RequestBody AddCommentRequest request) {
        try {
            postsManager.addComment(postId, request.content(), request.parentId());
            return ResponseEntity.ok().build();
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping(value = "/posts/comments/{commentId}")
    ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        try {
            postsManager.deleteComment(commentId);
            return ResponseEntity.ok().build();
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(value = "/posts/{postId}/comments")
    ResponseEntity<GetCommentsResponse> getHeadComments(@PathVariable Long postId, @RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        try {
            if (pageNum == null) {
                pageNum = 0;
            }
            if (pageSize == null) {
                pageSize = defaultPageSize;
            }
            List<PostCommentDTO> comments = postsManager.getHeadComments(postId, pageNum, pageSize);
            return ResponseEntity.ok(new GetCommentsResponse(comments));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping(value = "/posts/{postId}/comments/{commentId}")
    ResponseEntity<GetCommentsResponse> getChildComments(@PathVariable Long commentId, @RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        try {
            if (pageNum == null) {
                pageNum = 0;
            }
            if (pageSize == null) {
                pageSize = defaultPageSize;
            }
            List<PostCommentDTO> comments = postsManager.getChildComments(commentId, pageNum, pageSize);
            return ResponseEntity.ok(new GetCommentsResponse(comments));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
