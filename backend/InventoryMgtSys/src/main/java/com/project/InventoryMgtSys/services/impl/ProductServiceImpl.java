package com.project.InventoryMgtSys.services.impl;

import com.project.InventoryMgtSys.dtos.ProductDTO;
import com.project.InventoryMgtSys.dtos.Response;
import com.project.InventoryMgtSys.exceptions.InvalidOperationException;
import com.project.InventoryMgtSys.exceptions.NotFoundException;
import com.project.InventoryMgtSys.models.Category;
import com.project.InventoryMgtSys.models.Product;
import com.project.InventoryMgtSys.repositories.CategoryRepository;
import com.project.InventoryMgtSys.repositories.ProductRepository;
import com.project.InventoryMgtSys.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;

    @Value("${app.product.images-dir:product-images}")
    private String imageDirectory;

    @Value("${app.product.public-path:products}")
    private String publicImagePath;


    @Override
    public Response saveProduct(ProductDTO productDTO, MultipartFile imageFile) {

        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category Not Found"));

        //map our dto to product entity
        Product productToSave = Product.builder()
                .name(productDTO.getName())
                .sku(productDTO.getSku())
                .price(productDTO.getPrice())
                .stockQuantity(productDTO.getStockQuantity())
                .description(productDTO.getDescription())
                .category(category)
                .build();

        if (imageFile != null && !imageFile.isEmpty()) {
            log.info("Image file exist");
            productToSave.setImageUrl(saveImage(imageFile));
        }

        //save the product entity
        productRepository.save(productToSave);

        return Response.builder()
                .status(200)
                .message("Product successfully saved")
                .build();
    }

    @Override
    public Response updateProduct(ProductDTO productDTO, MultipartFile imageFile) {

        //check if product exisit
        Product existingProduct = productRepository.findById(productDTO.getProductId())
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        //check if image is associated with the product to update and upload
        if (imageFile != null && !imageFile.isEmpty()) {
            existingProduct.setImageUrl(saveImage(imageFile));
        }

        //check if category is to be chanegd for the products
        if (productDTO.getCategoryId() != null && productDTO.getCategoryId() > 0) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category Not Found"));
            existingProduct.setCategory(category);
        }

        //check if product fields is to be changed and update
        if (productDTO.getName() != null && !productDTO.getName().isBlank()) {
            existingProduct.setName(productDTO.getName());
        }

        if (productDTO.getSku() != null && !productDTO.getSku().isBlank()) {
            existingProduct.setSku(productDTO.getSku());
        }

        if (productDTO.getDescription() != null && !productDTO.getDescription().isBlank()) {
            existingProduct.setDescription(productDTO.getDescription());
        }

        if (productDTO.getPrice() != null && productDTO.getPrice().compareTo(BigDecimal.ZERO) >= 0) {
            existingProduct.setPrice(productDTO.getPrice());
        }

        if (productDTO.getStockQuantity() != null && productDTO.getStockQuantity() >= 0) {
            existingProduct.setStockQuantity(productDTO.getStockQuantity());
        }
        //update the product
        productRepository.save(existingProduct);

        //Build our response
        return Response.builder()
                .status(200)
                .message("Product Updated successfully")
                .build();


    }


    @Override
    public Response getAllProducts() {
        List<Product> productList = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<ProductDTO> productDTOList = productList.stream().map(product -> {
            ProductDTO dto = modelMapper.map(product, ProductDTO.class);

            // Determine stock status
            if (product.getStockQuantity() == 0) {
                dto.setStockStatus("Out of Stock");
            } else if (product.getStockQuantity() <= 5) {
                dto.setStockStatus("Low Stock");
            } else {
                dto.setStockStatus("In Stock");
            }

            return dto;
        }).toList();

        return Response.builder()
                .status(200)
                .message("success")
                .products(productDTOList)
                .build();
    }

    @Override
    public Response getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        return Response.builder()
                .status(200)
                .message("success")
                .product(modelMapper.map(product, ProductDTO.class))
                .build();
    }

    @Override
    public Response deleteProduct(Long id) {

        productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        productRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Product Deleted successfully")
                .build();
    }

    @Override
    public Response searchProduct(String input) {

        List<Product> products = productRepository.findByNameContainingOrDescriptionContaining(input, input);

        if (products.isEmpty()) {
            throw new NotFoundException("Product Not Found");
        }

        List<ProductDTO> productDTOList = modelMapper.map(products, new TypeToken<List<ProductDTO>>() {
        }.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .products(productDTOList)
                .build();
    }


    private String saveImage(MultipartFile imageFile) {
        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidOperationException("Only image files are allowed");
        }

        if (imageFile.getSize() > 5L * 1024 * 1024) {
            throw new InvalidOperationException("Image size must be less than or equal to 5MB");
        }

        try {
            Path uploadDirectory = Path.of(imageDirectory).toAbsolutePath().normalize();
            Files.createDirectories(uploadDirectory);

            String originalFilename = imageFile.getOriginalFilename() == null ? "product-image" : imageFile.getOriginalFilename();
            String uniqueFileName = UUID.randomUUID() + "_" + originalFilename.replaceAll("\\s+", "_");
            Path destinationFile = uploadDirectory.resolve(uniqueFileName);

            imageFile.transferTo(destinationFile);
            return publicImagePath + "/" + uniqueFileName;
        } catch (IOException e) {
            log.error("Unable to save product image", e);
            throw new InvalidOperationException("Unable to save product image");
        } catch (Exception e) {
            log.error("Unexpected error while saving product image", e);
            throw new InvalidOperationException("Unexpected error while saving product image");
        }
    }
}
