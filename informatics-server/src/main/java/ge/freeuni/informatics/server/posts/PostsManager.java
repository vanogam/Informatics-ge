package ge.freeuni.informatics.server.posts;

import ge.freeuni.informatics.common.dto.PostDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.post.Post;
import ge.freeuni.informatics.repository.contestroom.IContestRoomRepository;
import ge.freeuni.informatics.repository.post.IPostRepository;
import ge.freeuni.informatics.server.user.IUserManager;
import ge.freeuni.informatics.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

@Service
public class PostsManager implements IPostsManager {

    @Value("${ge.freeuni.informatics.Task.postImageDirectoryAddress}")
    String postImageDirectoryAddress;

    @Autowired
    IPostRepository postRepository;

    @Autowired
    IContestRoomRepository roomRepository;

    @Autowired
    IUserManager userManager;

    @Override
    public PostDTO getPost(long postId) throws InformaticsServerException {
        PostDTO postDTO = PostDTO.toDTO(postRepository.getPost(postId));
        ContestRoom room = roomRepository.getRoom(postDTO.getRoomId());
        if (!room.isMember(userManager.getAuthenticatedUser().getId())) {
            throw new InformaticsServerException("permissionDenied");
        }
        postDTO.setAuthorName(userManager.getUser(postDTO.getAuthorId()).getUsername());
        return postDTO;
    }

    @Override
    public List<PostDTO> getPosts(long roomId, Integer offset, Integer limit) throws InformaticsServerException {
        ContestRoom room = roomRepository.getRoom(roomId);
        if (!room.isMember(userManager.getAuthenticatedUser().getId())) {
            throw new InformaticsServerException("permissionDenied");
        }
        List<PostDTO> postDTOList = PostDTO.toDTOs(postRepository.filter(roomId, offset, limit));
        for (PostDTO postDTO : postDTOList) {
            postDTO.setAuthorName(userManager.getUser(postDTO.getAuthorId()).getUsername());
        }
        return postDTOList;
    }

    @Override
    public File getPostImage(long postId) throws InformaticsServerException {
        Post post = postRepository.getPost(postId);
        ContestRoom room = roomRepository.getRoom(post.getRoomId());
        Long currentUser = userManager.getAuthenticatedUser().getId();
        if (!room.isOpen() && !room.isMember(currentUser)) {
            throw new InformaticsServerException("permissionDenied");
        }

        return new File(post.getImagePath());
    }

    @Override
    public void addPost(PostDTO post) throws InformaticsServerException {
        post.setAuthorId(userManager.getAuthenticatedUser().getId());
        post.setPostDate(new Date());
        Post postEntity = PostDTO.fromDTO(post);
        Post entityAtDB = postRepository.getPost(post.getId());
        ContestRoom room = roomRepository.getRoom(post.getRoomId());
        if (!room.getTeachers().contains(userManager.getAuthenticatedUser().getId())) {
            throw new InformaticsServerException("permissionDenied");
        }
        if (entityAtDB != null) {
            if (entityAtDB.getRoomId() != postEntity.getRoomId()) {
                throw new InformaticsServerException("invalidAction");
            }
            postEntity.setImagePath(entityAtDB.getImagePath());
        }
        if (postEntity.getId() == null) {
            postEntity = postRepository.savePost(postEntity);
        }
    }

    @Override
    public void uploadImage(long postId, byte[] image) throws InformaticsServerException {
        Post post = postRepository.getPost(postId);
        Post entityAtDB = postRepository.getPost(post.getId());
        ContestRoom room = roomRepository.getRoom(post.getRoomId());
        if (!room.getTeachers().contains(userManager.getAuthenticatedUser().getId())) {
            throw new InformaticsServerException("permissionDenied");
        }

        try {
            post.setImagePath(storeImage(postId, image));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        postRepository.savePost(post);
    }

    private String storeImage(long postId, byte[] image) throws IOException, InformaticsServerException {
        String folder = FileUtils.buildPath(postImageDirectoryAddress, String.valueOf(postId));
        Files.createDirectories(Paths.get(folder));
        String fileAddress = FileUtils.buildPath(folder, FileUtils.getRandomFileName(10));
        File imageFile = new File(fileAddress);
        if (imageFile.isFile()) {
            boolean ignored = imageFile.delete();
        }
        if(!imageFile.createNewFile()) {
            throw new InformaticsServerException("Could not create statement");
        }
        OutputStream outputStream = Files.newOutputStream(imageFile.toPath());
        outputStream.write(image);
        return fileAddress;
    }
}
