package com.cineclubs.app.services;

import com.cineclubs.app.models.Club;
import com.cineclubs.app.models.Post;
import com.cineclubs.app.models.User;
import com.cineclubs.app.repository.PostRepository;
import com.cineclubs.app.dto.PostDTO;
import com.cineclubs.app.dto.PageRequest;
import com.cineclubs.app.dto.PageResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserService userService;
    private final ClubService clubService;
    private final SimpMessagingTemplate messagingTemplate;

    public PostService(PostRepository postRepository,
            UserService userService,
            ClubService clubService,
            SimpMessagingTemplate messagingTemplate) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.clubService = clubService;
        this.messagingTemplate = messagingTemplate;
    }

    public PostDTO createPost(Long clubId, String userId, Post post) {
        User author = userService.getUserByUserId(userId);
        Club club = clubService.getClubById(clubId);

        post.setAuthor(author);
        post.setClub(club);
        Post savedPost = postRepository.save(post);
        PostDTO postDTO = new PostDTO(savedPost);
        messagingTemplate.convertAndSend("/topic/posts", postDTO);
        return postDTO;
    }

    public List<PostDTO> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(PostDTO::new)
                .toList();
    }

    public PostDTO getPostById(Long id, String currentUserId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + id));
        return new PostDTO(post, currentUserId);
    }

    public PageResponse<PostDTO> getPostsForClub(Long clubId, String currentUserId, PageRequest pageRequest) {
        List<Post> posts;
        if (pageRequest.getCursor() == null) {
            posts = postRepository.findFirstByClubIdOrderByIdDesc(clubId, pageRequest.getLimit() + 1);
        } else {
            posts = postRepository.findByClubIdAndIdLessThanOrderByIdDesc(
                    clubId,
                    pageRequest.getCursor(),
                    pageRequest.getLimit() + 1);
        }

        boolean hasMore = posts.size() > pageRequest.getLimit();
        List<Post> postsToReturn = hasMore ? posts.subList(0, pageRequest.getLimit()) : posts;

        List<PostDTO> postDTOs = postsToReturn.stream()
                .map(post -> currentUserId != null ? new PostDTO(post, currentUserId) : new PostDTO(post))
                .toList();

        Long nextCursor = hasMore ? posts.get(pageRequest.getLimit() - 1).getId() : null;

        return new PageResponse<>(postDTOs, nextCursor, hasMore);
    }

    public List<PostDTO> getPostsForUser(String userId) {
        return postRepository.findByAuthorUserIdOrderByCreatedAtDesc(userId).stream()
                .map(PostDTO::new)
                .toList();
    }

    public PostDTO likePost(Long postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        User user = userService.getUserByUserId(userId);

        post.getLikes().add(user);
        Post likedPost = postRepository.save(post);
        PostDTO postDTO = new PostDTO(likedPost);
        postDTO.setHasLiked(true);
        messagingTemplate.convertAndSend("/topic/posts", postDTO);
        return postDTO;
    }

    public PostDTO unlikePost(Long postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        User user = userService.getUserByUserId(userId);
        post.getLikes().remove(user);
        Post unlikedPost = postRepository.save(post);
        PostDTO postDTO = new PostDTO(unlikedPost);
        messagingTemplate.convertAndSend("/topic/posts", postDTO);
        return postDTO;
    }

    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }
}
