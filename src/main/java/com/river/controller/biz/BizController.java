package com.river.controller.biz;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.river.service.ModelerService;
import com.river.util.ProcessDefinitionGeneratorEx;

/**
 * @author: he.feng
 * @date: 20:20 2017/11/30
 * @desc:
 **/
@Controller
@RequestMapping("/biz")
public class BizController {

	private static final Logger logger = LoggerFactory.getLogger(BizController.class);

	@Resource
	private ModelerService modelerService;

	@Autowired
	private ProcessEngine processEngine;//流程引擎对象

	@Autowired
	private RepositoryService repositoryService;//工作流仓储服务

	@Autowired
	private ProcessDefinitionGeneratorEx processDefinitionGeneratorEx;

	@Autowired
	private RuntimeService runtimeService;  


	@RequestMapping("/model/list")
	@ResponseBody
	public List modelList() {
		List<Model> list = modelerService.queryModelList();
		return list;
	}

	@RequestMapping("model/showPic")
	public void showPic(@RequestParam(value = "processInstanceId") String processInstanceId , HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		/*	InputStream inputStream = findProcessPic("27501");
		byte[] b = new byte[1024];
		int len = 0;
		while ((len = inputStream.read(b,0,1024))!=-1){
			response.getOutputStream().write(b, 0, len);
		}
		 */

		/*	InputStream inputStream = processDefinitionGeneratorEx.generateDiagramWithHighLight("30001");
		byte[] b = new byte[1024];
		int len = 0;
		while ((len = inputStream.read(b,0,1024))!=-1){
			response.getOutputStream().write(b, 0, len);
		}*/
		
		InputStream inputStream = getDiagram(processInstanceId);
		byte[] b = new byte[1024];
		int len = 0;
		while ((len = inputStream.read(b,0,1024))!=-1){
			response.getOutputStream().write(b, 0, len);
		}
		
	}


	private InputStream getDiagram(String processInstanceId){
		//查询流程实例
		ProcessInstance pi =this.runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		BpmnModel bpmnModel = this.repositoryService.getBpmnModel(pi.getProcessDefinitionId());
		//得到正在执行的环节
		List<String> activeIds = this.runtimeService.getActiveActivityIds(pi.getId());
		InputStream is = new DefaultProcessDiagramGenerator().generateDiagram(
				bpmnModel, "png",
				activeIds, Collections.<String>emptyList(),
				processEngine.getProcessEngineConfiguration().getActivityFontName(),
				processEngine.getProcessEngineConfiguration().getLabelFontName(),
				null,null, 1.0);
		return is;
	}



	public InputStream findProcessPic(String deploymentId) {
		ProcessDefinition definition = getProcessDefinition(deploymentId);
		String source = definition.getDiagramResourceName();
		InputStream inputStream = repositoryService.getResourceAsStream(definition.getDeploymentId(),source);
		return inputStream;
	}



	public ProcessDefinition  getProcessDefinition(String deploymentId)
	{
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
				.deploymentId(deploymentId).singleResult();

		return processDefinition;
	}






}
