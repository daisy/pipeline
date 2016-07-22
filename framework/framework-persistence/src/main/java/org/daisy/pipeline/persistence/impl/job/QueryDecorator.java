package org.daisy.pipeline.persistence.impl.job;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * This class makes easy to chain different criteria 
 * build queries against a database
 *
 */
public abstract class QueryDecorator<T> {
        final EntityManager em;//needed to get CriteriaBuilders
        private QueryDecorator<T> next;//next decorator in the chain

        public QueryDecorator(EntityManager em){
                this.em=em;
        }


        /**
         * Sets the next decorator
         * @param decorator
         * @return the parameter so can be called with next again
         */
        public final QueryDecorator<T> setNext(QueryDecorator<T> decorator){
                this.next=decorator;
                return this.next;

        }

        /**
         *  Builds the decorated select query 
         * @param clazz
         * @return
         */
        public final TypedQuery<T> getQuery(Class<T> clazz){
                //TODO: Is it possible to get rid of the clazz parameter?
                CriteriaBuilder cb = this.em.getCriteriaBuilder();
                CriteriaQuery<T> query=cb.createQuery(clazz);
                Root<T> root = query.from(clazz);
                //always true
                Predicate pred=cb.conjunction();
                pred=this.decorateWhere(this.holder(cb,root,query),pred);
                query.where(pred);
                return this.em.createQuery(query);
        }


        
        /**
         * Returns a new QueryHolder used internally to send 
         * the objects needed to build the query 
         *
         * @param cb
         * @param root
         * @param query
         * @return
         */
        final QueryHolder holder(CriteriaBuilder cb, Root<T> root,
                                CriteriaQuery<T> query){
                return new QueryHolder(cb,root,query); 
        }


        /**
         * Appends the predicated produced by this object and calls the next QueryDecorator
         *
         */
        final Predicate decorateWhere(QueryHolder holder, Predicate pred){
                //decorate predicates with and and go to the next decorator
                pred=holder.cb.and(pred,this.getPredicate(holder));

                if(this.next!=null){
                        pred=this.next.decorateWhere(holder,pred);
                }
                return pred;
        }


        /**
         * Returns the Predicate to be appended to the select statement
         *
         * @param holder
         * @return
         */
        abstract Predicate getPredicate(QueryHolder holder);


        /**
         * Holds the objects needed to create 
         * the predicates
         *
         */
        class QueryHolder{
                final CriteriaBuilder cb;
                final Root<T> root;
                final CriteriaQuery<T> query;

                /**
                 * @param cb
                 * @param root
                 * @param query
                 */
                public QueryHolder(CriteriaBuilder cb, Root<T> root,
                                CriteriaQuery<T> query) {
                        this.cb = cb;
                        this.root = root;
                        this.query = query;
                }
                
        }

        public static <T>  QueryDecorator<T> empty(EntityManager em){
                return new QueryDecorator<T>(em) {

                        @Override
                        Predicate getPredicate(
                                        QueryDecorator<T>.QueryHolder holder) {
                                //always returns true;
                                return holder.cb.conjunction();
                        }
                };
        }
}
