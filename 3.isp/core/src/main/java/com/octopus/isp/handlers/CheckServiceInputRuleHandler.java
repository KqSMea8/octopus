package com.octopus.isp.handlers;

import com.octopus.utils.alone.StringUtils;
import com.octopus.utils.cls.proxy.IMethodAddition;
import com.octopus.utils.exception.ISPException;
import com.octopus.utils.rule.RuleUtil;
import com.octopus.utils.xml.XMLMakeup;
import com.octopus.utils.xml.XMLObject;
import com.octopus.utils.xml.auto.ResultCheck;
import com.octopus.utils.xml.auto.XMLDoObject;
import com.octopus.utils.xml.auto.XMLParameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Created by Administrator on 2018/9/23.
 */
public class CheckServiceInputRuleHandler  extends XMLDoObject implements IMethodAddition {
    transient static Log log = LogFactory.getLog(CheckServiceInputRuleHandler.class);
    //Handler must exist these properties
    List<String> methods;
    boolean isWaitBefore;
    boolean isWaitAfter;
    boolean isWaitResult;
    boolean isNextInvoke;
    Map in=null;
    XMLDoObject cache_srv_rule;

    int level;
    static Map defineRuleElement=null;//在constant中定义的serviceinputcheckrules
    public CheckServiceInputRuleHandler(XMLMakeup xml, XMLObject parent, Object[] containers) throws Exception {
        super(xml, parent, containers);
        isWaitBefore= StringUtils.isTrue(xml.getProperties().getProperty("iswaitebefore"));
        isWaitAfter= StringUtils.isTrue(xml.getProperties().getProperty("iswaitafter"));
        isWaitResult= StringUtils.isTrue(xml.getProperties().getProperty("iswaitresult"));
        isNextInvoke= StringUtils.isTrue(xml.getProperties().getProperty("isnextinvoke"));

    }

    @Override
    public Object beforeAction(Object impl, String m, Object[] args) throws Exception {
        if(null != impl) {
            Map data = getDataMap(args);
            String id =((XMLObject) impl).getXML().getId();
            List<Map> rules = getRuleByActionId((XMLParameter)args[1],id);
            if(null != rules) {
                for(Map rule:rules) {
                    if (null !=rule && StringUtils.isNotBlank(rule.get("RULE"))) {
                        if (null != data) {
                            String mp = (String)rule.get("PARAMETER_MAPPING");
                            XMLParameter e = ((XMLParameter)args[1]);
                            if(StringUtils.isNotBlank(mp) && data instanceof XMLParameter){
                                Map d = ((XMLParameter)data).getMapValueFromParameter(StringUtils.convert2MapJSONObject(mp),this);
                                if(null != d){
                                    e = getEmptyParameter();
                                    e.putAll(d);
                                }

                            }

                            Object rul = e.getExpressValueFromMap((String)rule.get("RULE"),this);
                            if(rul instanceof String) {

                                Object o = RuleUtil.doRule((String) rul, data);
                                if (null != o && o instanceof Boolean && !(Boolean) o) {
                                    Object s = ((XMLParameter) args[1]).getValueFromExpress(rule.get("NOT_CHECK_MESSAGE"), this);
                                    if(null != s){
                                        s = s.toString();
                                        s = StringUtils.replace((String)s,"\"","\\\"");
                                        s = StringUtils.replace((String)s,"\\\\\"","\\\\\\\"");

                                    }
                                    throw new ISPException("600", "check [" + id + "] input parameters fail: " + s);
                                }
                            }
                        }
                    }
                }
            }
            //log.error(((XMLObject) impl).getXML().getId() +" "+m+ " do check input...");
        }
        return null;
    }

    //根据服务名称获取对应的入参检查规则
    List<Map> getRuleByActionId(XMLParameter env,String s){
        try {
            HashMap in = new HashMap();
            //in.put("cache", "cache_srv_rule");
            in.put("op", "get");
            in.put("key", s);
            List ret = (List)cache_srv_rule.doSomeThing(null, env, in, null, null);
            if(null != ret){
                return ret;
            }else {
                return null;
            }
        }catch (Exception e){
            log.error("get rule by serviceId fail",e);
        }
        return null;
    }
    //装载定义的规则元素
    Map getDataMap(Object[] args){
        if(args.length>1) {
            if(args[1] instanceof Map) {
                Map m = (Map) args[1];
                if(null == defineRuleElement){
                    defineRuleElement=initDefineRuleElement(m);
                }
                if(null != defineRuleElement) {
                    m.putAll(defineRuleElement);
                }
                return m;
            }
        }
        return null;
    }
    Map initDefineRuleElement(Map m){
        Map c = (Map)m.get("${constant}");
        if(null != c){
            HashMap ret = new HashMap();
            Map s = (Map)c.get("serviceinputcheckrules");
            if(null != s){
                Iterator it = s.keySet().iterator();
                while(it.hasNext()){
                    String k = (String)it.next();
                    String v = (String)s.get(k);
                    if(StringUtils.isNotBlank(k) && StringUtils.isNotBlank(v)){

                            try {
                                Object re = Class.forName(v).newInstance();
                                ret.put(k,re);
                            } catch (InstantiationException e) {
                                log.error(e);
                            } catch (IllegalAccessException e) {
                                log.error(e);
                            } catch (ClassNotFoundException e) {
                                log.error(e);
                            }

                    }
                }
            }
            if(ret.size()>0) return ret;
        }
        return null;
    }

    @Override
    public Object afterAction(Object impl, String m, Object[] args, boolean isInvoke, boolean isSuccess, Throwable e, Object result) {
        return null;
    }

    @Override
    public Object resultAction(Object impl, String m, Object[] args, Object result) {
        return null;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public boolean isWaiteBefore() {
        return isWaitBefore;
    }

    @Override
    public boolean isWaiteAfter() {
        return isWaitAfter;
    }

    @Override
    public boolean isWaiteResult() {
        return isWaitResult;
    }

    @Override
    public boolean isNextInvoke() {
        return isNextInvoke;
    }

    @Override
    public void setMethods(List<String> methods) {
        this.methods=methods;
    }

    @Override
    public List<String> getMethods() {
        return methods;
    }

    @Override
    public void doInitial() throws Exception {

    }

    @Override
    public boolean checkInput(String xmlid, XMLParameter env, Map input, Map output, Map config) throws Exception {
        return true;
    }

    @Override
    public Object doSomeThing(String xmlid, XMLParameter env, Map input, Map output, Map config) throws Exception {
        return null;
    }

    @Override
    public ResultCheck checkReturn(String xmlid, XMLParameter env, Map input, Map output, Map config, Object ret) throws Exception {
        return null;
    }

    @Override
    public boolean commit(String xmlid, XMLParameter env, Map input, Map output, Map config, Object ret) throws Exception {
        return false;
    }

    @Override
    public boolean rollback(String xmlid, XMLParameter env, Map input, Map output, Map config, Object ret, Exception e) throws Exception {
        return false;
    }
}
