package com.godot.community.controller;

import com.godot.community.service.AlphaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "Hello Spring Boot";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getData() {
        return alphaService.find();
    }

    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response) {
        // get request
        // request line
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        // header
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ": " + value);
        }
        // content
        System.out.println(request.getParameter("code"));

        // response
        response.setContentType("text/html;charset=utf-8");
        try (PrintWriter writer = response.getWriter();) {
            writer.write("<h1>blog</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // GET
    // /students?
    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            @RequestParam(name = "current", required = false, defaultValue = "1") int current,
            @RequestParam(name = "limit", required = true, defaultValue = "10") int limit)
    {
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }
    // /student/123
    @RequestMapping(path = "/student/{id}",method = RequestMethod.GET)
    @ResponseBody
    public  String getStudent(@PathVariable("id")int id){
        System.out.println(id);
        return "a student";
    }


    // POST
    @RequestMapping(path = "/student",method = RequestMethod.POST)
    @ResponseBody
    public  String saveStudent(String name, int age){
        System.out.println(name);
        System.out.println(age);
        return "success";
    }


    // Response html
    @RequestMapping(path = "/teacher",method = RequestMethod.GET)
    public ModelAndView getTeacher(){
        ModelAndView mav= new ModelAndView();
        mav.addObject("name","laowang");
        mav.addObject("age","30");
        mav.setViewName("/demo/view");
        return mav;
    }
    @RequestMapping(path = "/school",method = RequestMethod.GET)
    public  String getSchool(Model model){
       model.addAttribute("name","sctb");
       model.addAttribute("age",20);
       return "/demo/view";
    }

    // Response json (asynchronous request need)
    // java obj -> json string -> jsp obj
    @RequestMapping(path = "/emp",method = RequestMethod.GET)
    @ResponseBody   // without this will be assumed as return *html* instead of *json by browser
    public Map<String, Object> getEmp(){
        Map<String,Object> emp = new HashMap<>();
        emp.put("name","laowang");
        emp.put("age",23);
        emp.put("salary",3000.0);
        return emp;
    }

    @RequestMapping(path = "/emps",method = RequestMethod.GET)
    @ResponseBody   // without this will be assumed as return *html* instead of *json by browser
    public List<Map<String, Object>> getEmps(){
        List<Map<String,Object>>   list = new ArrayList<>();

        Map<String,Object> emp = new HashMap<>();
        emp.put("name","laowang");
        emp.put("age",23);
        emp.put("salary",3000.0);
        list.add(emp);

        emp=new HashMap<>();
        emp.put("name","lisi");
        emp.put("age",24);
        emp.put("salary",3123.34);
        list.add(emp);

        emp=new HashMap<>();
        emp.put("name","gg");
        emp.put("age",344);
        emp.put("salary",34212);
        list.add(emp);

        return list;
    }
}
