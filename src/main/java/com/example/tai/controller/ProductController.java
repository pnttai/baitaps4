package com.example.tai.controller;


import com.example.tai.model.Product;
import com.example.tai.service.CategoryService;
import com.example.tai.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService; // Đảm bảo bạn đã inject CategoryService
// Display a list of all products
    @GetMapping
    public String showProductList(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "/products/product-list";
    }
    // For adding a new product
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories()); //Load categories
        return "/products/add-product";
    }
    // Process the form for adding a new product
    @PostMapping("/add")
    public String addProduct(@Valid Product product, BindingResult result, @RequestParam("image") MultipartFile imageFile) {
        if (result.hasErrors()) {
            return "/products/add-product";
        }
        if(!imageFile.isEmpty()){
            try{
                String imageName = saveImageStatic(imageFile);
                product.setThumbnail("/images/" + imageName);
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }
        productService.addProduct(product);
        return "redirect:/products";
    }

    private String saveImageStatic(MultipartFile imageFile) throws IOException{
        File saveFile = new ClassPathResource("static/images").getFile();
        String fileName = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(imageFile.getOriginalFilename());
        Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + fileName);
        Files.copy(imageFile.getInputStream(), path);
        return fileName;
    }

    // For editing a product
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories()); //Load categories
        return "/products/update-product";
    }
    // Process the form for updating a product
        @PostMapping("/update/{id}")
        public String updateProduct(@PathVariable Long id, @Valid Product product, BindingResult result, @RequestParam("image") MultipartFile imageFile) {
            if (result.hasErrors()) {
                product.setId(id);
                return "/products/update-product";
            }
            if (!imageFile.isEmpty()) {
                try {

                    String imageName = saveImageStatic(imageFile);
                    product.setThumbnail("/images/" + imageName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            productService.updateProduct(product);
            return "redirect:/products";
        }
    // Handle request to delete a product
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        return "redirect:/products";
    }
    @GetMapping("/search")
    public String searchByName(@RequestParam("keyword") String keyword, Model model){
        List<Product> products = productService.getAllProducts()
                .stream()
                .filter(p-> p.getName().toLowerCase().contains(keyword.toLowerCase())).collect(Collectors.toList());
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        return "/products/product-list";
    }
}